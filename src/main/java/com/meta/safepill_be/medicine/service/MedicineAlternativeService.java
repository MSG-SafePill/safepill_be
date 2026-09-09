package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.common.service.GeminiService;
import com.meta.safepill_be.medicine.domain.IngredientMaster;
import com.meta.safepill_be.medicine.domain.InteractionRule;
import com.meta.safepill_be.medicine.domain.MedicineIngredient;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementIngredient;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.LlmAlternativeResponseDto;
import com.meta.safepill_be.medicine.dto.MedicineAlternativeDto;
import com.meta.safepill_be.medicine.repository.IngredientMasterRepository;
import com.meta.safepill_be.medicine.repository.InteractionRuleRepository;
import com.meta.safepill_be.medicine.repository.MedicineIngredientRepository;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 특정 의약품과 같은 성분을 공유하는 다른 의약품(대체 후보)을 찾고,
 * 로그인한 사용자의 마이약장과 교차해 병용주의 여부까지 함께 보여준다.
 */
@Service
@RequiredArgsConstructor
public class MedicineAlternativeService {
    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;
    private final MedicineIngredientRepository medicineIngredientRepository;
    private final InteractionRuleRepository interactionRuleRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final UserRepository userRepository;
    private final IngredientMasterRepository ingredientMasterRepository;
    private final GeminiService geminiService;

    @Transactional(readOnly = true)
    public List<MedicineAlternativeDto> findAlternatives(Long medicineId, String loginId) {
        MedicineMaster target = medicineMasterRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. id=" + medicineId));

        Map<Long, String> cabinetIngredientOwners = collectCabinetIngredientOwners(loginId);

        List<Long> targetIngredientIds = target.getIngredients().stream()
                .map(mi -> mi.getIngredientMaster().getId())
                .distinct()
                .toList();
        if (targetIngredientIds.isEmpty()) {
            // DB에 성분 데이터가 없는 약은 성분 매칭이 불가능하므로 AI 추천으로 대체
            return findAiSuggestedAlternatives(target, cabinetIngredientOwners);
        }

        Map<Long, MedicineMaster> candidatesById = new LinkedHashMap<>();
        Map<Long, Set<String>> sharedIngredientNamesById = new HashMap<>();
        for (Long ingredientId : targetIngredientIds) {
            for (MedicineIngredient link : medicineIngredientRepository.findTop10ByIngredientMaster_Id(ingredientId)) {
                MedicineMaster candidate = link.getMedicineMaster();
                if (candidate.getId().equals(medicineId)) {
                    continue;
                }
                candidatesById.putIfAbsent(candidate.getId(), candidate);
                sharedIngredientNamesById
                        .computeIfAbsent(candidate.getId(), key -> new LinkedHashSet<>())
                        .add(link.getIngredientMaster().getIngredientName());
            }
        }
        if (candidatesById.isEmpty()) {
            // DB상 성분을 공유하는 다른 약이 없는 경우에도 AI 추천으로 대체
            return findAiSuggestedAlternatives(target, cabinetIngredientOwners);
        }

        Set<Long> allIngredientIds = new HashSet<>(cabinetIngredientOwners.keySet());
        candidatesById.values().forEach(candidate -> candidate.getIngredients()
                .forEach(mi -> allIngredientIds.add(mi.getIngredientMaster().getId())));
        List<InteractionRule> rules = allIngredientIds.isEmpty()
                ? List.of()
                : interactionRuleRepository.findInteractionsByIngredientIds(new ArrayList<>(allIngredientIds));

        List<MedicineAlternativeDto> result = new ArrayList<>();
        for (MedicineMaster candidate : candidatesById.values()) {
            List<String> conflictReasons = findConflictReasons(candidate, cabinetIngredientOwners, rules);
            result.add(MedicineAlternativeDto.builder()
                    .id(candidate.getId())
                    .medicineName(candidate.getMedicineName())
                    .manufacturer(candidate.getMedicineManufacturer())
                    .sharedIngredients(new ArrayList<>(sharedIngredientNamesById.get(candidate.getId())))
                    .hasCabinetConflict(!conflictReasons.isEmpty())
                    .conflictReasons(conflictReasons)
                    .build());
        }

        // 마이약장과 충돌 없는 후보를 먼저, 그중에서도 공유 성분이 많은 순으로 정렬
        result.sort(Comparator
                .comparing(MedicineAlternativeDto::isHasCabinetConflict)
                .thenComparing((MedicineAlternativeDto dto) -> -dto.getSharedIngredients().size()));
        return result;
    }

    /**
     * DB 성분 매칭으로 대체 후보를 찾지 못했을 때 Gemini에게 대신 물어보는 fallback.
     * DB 검증된 결과가 아니므로 isAiSuggested=true로 명확히 표시해서 프론트에서 구분 노출한다.
     */
    private List<MedicineAlternativeDto> findAiSuggestedAlternatives(
            MedicineMaster target,
            Map<Long, String> cabinetIngredientOwners
    ) {
        LlmAlternativeResponseDto response = geminiService.askAlternatives(target.getMedicineName());
        if (response == null || response.getAlternatives() == null || response.getAlternatives().isEmpty()) {
            return List.of();
        }

        List<MedicineAlternativeDto> result = new ArrayList<>();
        for (LlmAlternativeResponseDto.Item item : response.getAlternatives()) {
            if (item.getName() == null || item.getName().isBlank()) {
                continue;
            }
            List<String> conflictReasons = List.of();
            if (item.getActiveIngredient() != null && !cabinetIngredientOwners.isEmpty()) {
                Optional<IngredientMaster> matched =
                        ingredientMasterRepository.findByIngredientName(item.getActiveIngredient().trim());
                if (matched.isPresent() && cabinetIngredientOwners.containsKey(matched.get().getId())) {
                    conflictReasons = List.of(
                            item.getActiveIngredient() + " 성분이 현재 복용 중인 '"
                                    + cabinetIngredientOwners.get(matched.get().getId())
                                    + "'와(과) 동일합니다. 중복 복용에 주의하세요."
                    );
                }
            }
            result.add(MedicineAlternativeDto.builder()
                    .id(null)
                    .medicineName(item.getName())
                    .manufacturer(null)
                    .sharedIngredients(item.getActiveIngredient() != null
                            ? List.of(item.getActiveIngredient()) : List.of())
                    .hasCabinetConflict(!conflictReasons.isEmpty())
                    .conflictReasons(conflictReasons)
                    .isAiSuggested(true)
                    .aiReason(item.getReason())
                    .build());
        }
        return result;
    }

    private List<String> findConflictReasons(
            MedicineMaster candidate,
            Map<Long, String> cabinetIngredientOwners,
            List<InteractionRule> rules
    ) {
        if (cabinetIngredientOwners.isEmpty() || rules.isEmpty()) {
            return List.of();
        }
        Set<Long> candidateIngredientIds = candidate.getIngredients().stream()
                .map(mi -> mi.getIngredientMaster().getId())
                .collect(Collectors.toSet());

        List<String> reasons = new ArrayList<>();
        for (InteractionRule rule : rules) {
            Long idA = rule.getIngredientA().getId();
            Long idB = rule.getIngredientB().getId();
            String cabinetItemName = null;
            String candidateIngredientName = null;
            if (candidateIngredientIds.contains(idA) && cabinetIngredientOwners.containsKey(idB)) {
                candidateIngredientName = rule.getIngredientA().getIngredientName();
                cabinetItemName = cabinetIngredientOwners.get(idB);
            } else if (candidateIngredientIds.contains(idB) && cabinetIngredientOwners.containsKey(idA)) {
                candidateIngredientName = rule.getIngredientB().getIngredientName();
                cabinetItemName = cabinetIngredientOwners.get(idA);
            }
            if (cabinetItemName != null) {
                reasons.add(candidateIngredientName + " 성분이 현재 복용 중인 '" + cabinetItemName + "'와(과) 병용 주의 대상입니다.");
            }
        }
        return reasons;
    }

    private Map<Long, String> collectCabinetIngredientOwners(String loginId) {
        Map<Long, String> owners = new HashMap<>();
        if (loginId == null || loginId.isBlank()) {
            return owners;
        }
        Optional<User> userOpt = userRepository.findByLoginId(loginId);
        if (userOpt.isEmpty()) {
            return owners;
        }
        List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(userOpt.get().getId());

        List<Long> medicineIds = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.MEDICINE)
                .map(UserMedicationReg::getItemId)
                .distinct()
                .toList();
        if (!medicineIds.isEmpty()) {
            for (MedicineMaster medicine : medicineMasterRepository.findByIdIn(medicineIds)) {
                for (MedicineIngredient mi : medicine.getIngredients()) {
                    owners.putIfAbsent(mi.getIngredientMaster().getId(), medicine.getMedicineName());
                }
            }
        }

        List<Long> supplementIds = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.SUPPLEMENT)
                .map(UserMedicationReg::getItemId)
                .distinct()
                .toList();
        if (!supplementIds.isEmpty()) {
            for (SupplementMaster supplement : supplementMasterRepository.findByIdIn(supplementIds)) {
                for (SupplementIngredient si : supplement.getIngredients()) {
                    owners.putIfAbsent(si.getIngredientMaster().getId(), supplement.getSupplementName());
                }
            }
        }
        return owners;
    }
}
