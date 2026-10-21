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
import com.meta.safepill_be.medicine.dto.MedicineAlternativeResponseDto;
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

import java.math.BigDecimal;
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
 * 특정 의약품의 대체 후보를 찾는다.
 * 1) 같은 성분을 가진 약(DB)  2) 같은 약효군(ATC)의 약(DB)  3) 둘 다 없을 때만 Gemini 추천(DB로 실존 대조).
 * 허가가 취하·만료된 약과 이미 마이약장에 있는 약은 추천하지 않고, 마이약장과의 병용 주의를 함께 보여준다.
 */
@Service
@RequiredArgsConstructor
public class MedicineAlternativeService {
    private static final int MAX_RESULTS = 10;
    private static final int MIN_RESULTS_BEFORE_EFFICACY_GROUP = 3;
    private static final int MAX_EFFICACY_GROUP_RESULTS = 5;
    private static final int MAX_CANDIDATES_TO_LOAD = 60;
    private static final int MAX_PARTIAL_RESULTS = 5;

    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;
    private final MedicineIngredientRepository medicineIngredientRepository;
    private final InteractionRuleRepository interactionRuleRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final UserRepository userRepository;
    private final IngredientMasterRepository ingredientMasterRepository;
    private final GeminiService geminiService;

    private record Owner(Long medicineId, String name) {}

    private record Cabinet(Map<Long, List<Owner>> ingredientOwners, Set<Long> medicineIds) {
        /** 성분 ID -> 마이약장에서 그 성분을 가진 약 이름. 교체 대상인 기준 약 자신은 제외한다. */
        Map<Long, String> ownerNames(Long excludeMedicineId) {
            Map<Long, String> names = new HashMap<>();
            ingredientOwners.forEach((ingredientId, owners) -> owners.stream()
                    .filter(owner -> owner.medicineId() == null || !owner.medicineId().equals(excludeMedicineId))
                    .findFirst()
                    .ifPresent(owner -> names.put(ingredientId, owner.name())));
            return names;
        }
    }

    private record Candidate(MedicineMaster medicine, String matchType) {}

    @Transactional(readOnly = true)
    public MedicineAlternativeResponseDto findAlternatives(Long medicineId, String loginId) {
        MedicineMaster target = medicineMasterRepository.findById(medicineId)
                .orElseThrow(() -> new IllegalArgumentException("의약품을 찾을 수 없습니다. id=" + medicineId));
        Cabinet cabinet = collectCabinet(loginId);
        Map<Long, MedicineIngredient> targetLinks = linksByIngredientId(target);

        List<Candidate> candidates = new ArrayList<>();
        if (!targetLinks.isEmpty()) {
            candidates.addAll(findIngredientCandidates(target, targetLinks, cabinet));
            if (candidates.size() < MIN_RESULTS_BEFORE_EFFICACY_GROUP) {
                candidates.addAll(findEfficacyGroupCandidates(target, cabinet, candidates));
            }
        }
        if (!candidates.isEmpty()) {
            return response(target, buildDtos(candidates, targetLinks, cabinet, target.getId()), false);
        }
        return findAiSuggestedAlternatives(target, cabinet);
    }

    private List<Candidate> findIngredientCandidates(
            MedicineMaster target,
            Map<Long, MedicineIngredient> targetLinks,
            Cabinet cabinet
    ) {
        Set<Long> targetIds = targetLinks.keySet();
        List<Long> candidateIds = medicineIngredientRepository.findCandidateMedicineIds(targetIds, target.getId())
                .stream().limit(MAX_CANDIDATES_TO_LOAD).toList();
        if (candidateIds.isEmpty()) {
            return List.of();
        }
        List<Candidate> result = new ArrayList<>();
        for (MedicineMaster candidate : medicineMasterRepository.findByIdIn(candidateIds)) {
            if (!isRecommendable(candidate, target, cabinet)) {
                continue;
            }
            Set<Long> candidateIds2 = ingredientIds(candidate);
            Set<Long> shared = new HashSet<>(candidateIds2);
            shared.retainAll(targetIds);
            if (shared.isEmpty()) {
                continue;
            }
            if (candidateIds2.equals(targetIds)) {
                result.add(new Candidate(candidate, MedicineAlternativeDto.SAME_INGREDIENT));
            } else {
                result.add(new Candidate(candidate, MedicineAlternativeDto.PARTIAL_INGREDIENT));
            }
        }
        return result;
    }

    /** ATC 4단계(약효·화학 소분류)가 같은 약. 성분이 달라도 비슷한 효능을 내는 약이다. */
    private List<Candidate> findEfficacyGroupCandidates(
            MedicineMaster target,
            Cabinet cabinet,
            List<Candidate> alreadyFound
    ) {
        String atc = target.getAtcCode();
        if (atc == null || atc.length() < 4) {
            return List.of();
        }
        String prefix = atc.substring(0, Math.min(5, atc.length()));
        if (prefix.endsWith("X")) {
            return List.of(); // ATC의 X는 '기타' 분류라 서로 다른 약이 섞인다
        }
        Set<Long> excluded = alreadyFound.stream().map(c -> c.medicine().getId()).collect(Collectors.toSet());
        return medicineMasterRepository.findTop30ByAtcCodeStartingWith(prefix).stream()
                .filter(candidate -> !excluded.contains(candidate.getId()))
                .filter(candidate -> isRecommendable(candidate, target, cabinet))
                .filter(candidate -> !candidate.getIngredients().isEmpty())
                .limit(MAX_EFFICACY_GROUP_RESULTS)
                .map(candidate -> new Candidate(candidate, MedicineAlternativeDto.SAME_EFFICACY_GROUP))
                .toList();
    }

    private boolean isRecommendable(MedicineMaster candidate, MedicineMaster target, Cabinet cabinet) {
        return !candidate.getId().equals(target.getId())
                && candidate.isPermitActive()
                && !candidate.getMedicineName().contains("수출용")
                && !cabinet.medicineIds().contains(candidate.getId());
    }

    private List<MedicineAlternativeDto> buildDtos(
            List<Candidate> candidates,
            Map<Long, MedicineIngredient> targetLinks,
            Cabinet cabinet,
            Long targetId
    ) {
        Map<Long, String> owners = cabinet.ownerNames(targetId);
        Set<Long> ruleIngredientIds = new HashSet<>(owners.keySet());
        candidates.forEach(c -> ruleIngredientIds.addAll(ingredientIds(c.medicine())));
        List<InteractionRule> rules = loadRules(ruleIngredientIds);

        List<MedicineAlternativeDto> dtos = new ArrayList<>();
        for (Candidate candidate : candidates) {
            MedicineMaster medicine = candidate.medicine();
            List<String> conflictReasons = cabinetWarnings(medicine, owners, rules);
            Map<Long, MedicineIngredient> candidateLinks = linksByIngredientId(medicine);

            List<String> shared = new ArrayList<>();
            boolean dosageDiffers = false;
            for (Map.Entry<Long, MedicineIngredient> entry : candidateLinks.entrySet()) {
                MedicineIngredient targetLink = targetLinks.get(entry.getKey());
                if (targetLink == null) {
                    continue;
                }
                shared.add(entry.getValue().getIngredientMaster().getIngredientName());
                BigDecimal a = targetLink.getDosage();
                BigDecimal b = entry.getValue().getDosage();
                if (a != null && b != null && a.compareTo(b) != 0) {
                    dosageDiffers = true;
                }
            }
            dtos.add(MedicineAlternativeDto.builder()
                    .id(medicine.getId())
                    .medicineName(medicine.getMedicineName())
                    .manufacturer(medicine.getMedicineManufacturer())
                    .matchType(candidate.matchType())
                    .sharedIngredients(shared)
                    .ingredientSummaries(summaries(medicine))
                    .dosageDiffers(dosageDiffers)
                    .hasCabinetConflict(!conflictReasons.isEmpty())
                    .conflictReasons(conflictReasons)
                    .build());
        }
        int targetSize = targetLinks.size();
        dtos.sort(Comparator
                .comparingInt((MedicineAlternativeDto dto) -> matchRank(dto.getMatchType()))
                .thenComparing(MedicineAlternativeDto::isHasCabinetConflict)
                .thenComparing(MedicineAlternativeDto::isDosageDiffers)
                .thenComparing((MedicineAlternativeDto dto) -> -(double) dto.getSharedIngredients().size() / targetSize)
                .thenComparingInt((MedicineAlternativeDto dto) ->
                        dto.getIngredientSummaries().size() - dto.getSharedIngredients().size())
                .thenComparing(MedicineAlternativeDto::getMedicineName));
        long partialSeen = 0;
        List<MedicineAlternativeDto> limited = new ArrayList<>();
        for (MedicineAlternativeDto dto : dtos) {
            if (MedicineAlternativeDto.PARTIAL_INGREDIENT.equals(dto.getMatchType()) && ++partialSeen > MAX_PARTIAL_RESULTS) {
                continue;
            }
            limited.add(dto);
        }
        dtos = limited;
        return dtos.size() > MAX_RESULTS ? new ArrayList<>(dtos.subList(0, MAX_RESULTS)) : dtos;
    }

    private int matchRank(String matchType) {
        return switch (matchType) {
            case MedicineAlternativeDto.SAME_INGREDIENT -> 0;
            case MedicineAlternativeDto.PARTIAL_INGREDIENT -> 1;
            case MedicineAlternativeDto.SAME_EFFICACY_GROUP -> 2;
            default -> 3;
        };
    }

    /**
     * DB에서 대체 후보를 찾지 못했을 때 Gemini에게 대신 묻는 마지막 수단.
     * AI가 지어낸 이름일 수 있어 DB에 실제 있는 약인지 대조하고, 허가가 끝난 약은 버린다.
     */
    private MedicineAlternativeResponseDto findAiSuggestedAlternatives(MedicineMaster target, Cabinet cabinet) {
        LlmAlternativeResponseDto response = geminiService.askAlternatives(target.getMedicineName());
        if (response == null) {
            return response(target, List.of(), true);
        }
        if (response.getAlternatives() == null || response.getAlternatives().isEmpty()) {
            return response(target, List.of(), false);
        }

        record AiPick(LlmAlternativeResponseDto.Item item, MedicineMaster dbMatch) {}
        List<AiPick> picks = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (LlmAlternativeResponseDto.Item item : response.getAlternatives()) {
            if (item.getName() == null || item.getName().isBlank()
                    || isSameProduct(item.getName(), target.getMedicineName())
                    || !seen.add(normalizeName(item.getName()))) {
                continue;
            }
            List<MedicineMaster> dbMatches = medicineMasterRepository
                    .findTop5ByMedicineNameContainingIgnoreCase(item.getName().trim());
            MedicineMaster dbMatch = null;
            if (!dbMatches.isEmpty()) {
                dbMatch = dbMatches.stream()
                        .filter(m -> isRecommendable(m, target, cabinet))
                        .findFirst()
                        .orElse(null);
                if (dbMatch == null) {
                    continue; // 우리 DB에 있지만 허가가 끝났거나 이미 복용 중인 약
                }
            }
            picks.add(new AiPick(item, dbMatch));
        }

        Map<Long, String> owners = cabinet.ownerNames(target.getId());
        Set<Long> ruleIngredientIds = new HashSet<>(owners.keySet());
        picks.stream().map(AiPick::dbMatch).filter(m -> m != null).forEach(m -> ruleIngredientIds.addAll(ingredientIds(m)));
        List<InteractionRule> rules = loadRules(ruleIngredientIds);

        List<MedicineAlternativeDto> result = new ArrayList<>();
        for (AiPick pick : picks) {
            LlmAlternativeResponseDto.Item item = pick.item();
            MedicineMaster dbMatch = pick.dbMatch();
            List<String> conflictReasons;
            if (dbMatch != null && !dbMatch.getIngredients().isEmpty()) {
                conflictReasons = cabinetWarnings(dbMatch, owners, rules);
            } else {
                conflictReasons = conflictByIngredientName(item.getActiveIngredient(), owners);
            }
            List<String> ingredientNames = dbMatch != null && !dbMatch.getIngredients().isEmpty()
                    ? summaries(dbMatch)
                    : (item.getActiveIngredient() != null ? List.of(item.getActiveIngredient()) : List.of());
            result.add(MedicineAlternativeDto.builder()
                    .id(dbMatch != null ? dbMatch.getId() : null)
                    .medicineName(dbMatch != null ? dbMatch.getMedicineName() : item.getName().trim())
                    .manufacturer(dbMatch != null ? dbMatch.getMedicineManufacturer() : null)
                    .matchType(MedicineAlternativeDto.AI_SUGGESTED)
                    .sharedIngredients(item.getActiveIngredient() != null ? List.of(item.getActiveIngredient()) : List.of())
                    .ingredientSummaries(ingredientNames)
                    .hasCabinetConflict(!conflictReasons.isEmpty())
                    .conflictReasons(conflictReasons)
                    .isAiSuggested(true)
                    .isVerifiedInDb(dbMatch != null)
                    .aiReason(item.getReason())
                    .build());
        }
        result.sort(Comparator.comparing(MedicineAlternativeDto::isHasCabinetConflict)
                .thenComparing(dto -> !dto.isVerifiedInDb()));
        return response(target, result, false);
    }

    private List<String> conflictByIngredientName(String ingredientName, Map<Long, String> cabinetOwners) {
        if (ingredientName == null || cabinetOwners.isEmpty()) {
            return List.of();
        }
        Optional<IngredientMaster> matched =
                ingredientMasterRepository.findFirstByIngredientNameOrderByIdAsc(ingredientName.trim());
        if (matched.isPresent() && cabinetOwners.containsKey(matched.get().getId())) {
            return List.of(ingredientName + " 성분이 현재 복용 중인 '"
                    + cabinetOwners.get(matched.get().getId()) + "'와(과) 동일합니다. 중복 복용에 주의하세요.");
        }
        return List.of();
    }

    /** 마이약장과의 병용 주의 규칙 + 이미 복용 중인 약과 같은 성분이 겹치는 경우(중복 복용). */
    private List<String> cabinetWarnings(
            MedicineMaster candidate,
            Map<Long, String> cabinetOwners,
            List<InteractionRule> rules
    ) {
        List<String> warnings = new ArrayList<>();
        for (MedicineIngredient link : candidate.getIngredients()) {
            String owner = cabinetOwners.get(link.getIngredientMaster().getId());
            if (owner != null) {
                warnings.add(link.getIngredientMaster().getIngredientName() + " 성분이 이미 복용 중인 '"
                        + owner + "'에도 들어 있어요. 중복 복용에 주의하세요.");
            }
        }
        warnings.addAll(findConflictReasons(candidate, cabinetOwners, rules));
        return warnings.stream().distinct().toList();
    }

    private List<String> findConflictReasons(
            MedicineMaster candidate,
            Map<Long, String> cabinetIngredientOwners,
            List<InteractionRule> rules
    ) {
        if (cabinetIngredientOwners.isEmpty() || rules.isEmpty()) {
            return List.of();
        }
        Set<Long> candidateIngredientIds = ingredientIds(candidate);
        Set<String> reasons = new LinkedHashSet<>();
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
        return new ArrayList<>(reasons);
    }

    private List<InteractionRule> loadRules(Set<Long> ingredientIds) {
        return ingredientIds.isEmpty()
                ? List.of()
                : interactionRuleRepository.findInteractionsByIngredientIds(new ArrayList<>(ingredientIds));
    }

    private MedicineAlternativeResponseDto response(
            MedicineMaster target,
            List<MedicineAlternativeDto> alternatives,
            boolean aiUnavailable
    ) {
        return MedicineAlternativeResponseDto.builder()
                .target(MedicineAlternativeResponseDto.Target.builder()
                        .id(target.getId())
                        .medicineName(target.getMedicineName())
                        .manufacturer(target.getMedicineManufacturer())
                        .ingredients(summaries(target))
                        .permitActive(target.isPermitActive())
                        .permitStatus(target.getCancelName())
                        .cancelDate(formatDate(target.getCancelDate()))
                        .build())
                .alternatives(alternatives)
                .aiUnavailable(aiUnavailable)
                .build();
    }

    private Map<Long, MedicineIngredient> linksByIngredientId(MedicineMaster medicine) {
        Map<Long, MedicineIngredient> links = new LinkedHashMap<>();
        for (MedicineIngredient link : medicine.getIngredients()) {
            links.putIfAbsent(link.getIngredientMaster().getId(), link);
        }
        return links;
    }

    private Set<Long> ingredientIds(MedicineMaster medicine) {
        return medicine.getIngredients().stream()
                .map(link -> link.getIngredientMaster().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> summaries(MedicineMaster medicine) {
        return linksByIngredientId(medicine).values().stream().map(link -> {
            String name = link.getIngredientMaster().getIngredientName();
            if (link.getDosage() == null) {
                return name;
            }
            String unit = link.getIngredientMaster().getUnit();
            return name + " " + link.getDosage().stripTrailingZeros().toPlainString() + (unit == null ? "" : unit);
        }).toList();
    }

    private String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || !yyyymmdd.matches("\\d{8}")) {
            return yyyymmdd;
        }
        return yyyymmdd.substring(0, 4) + "-" + yyyymmdd.substring(4, 6) + "-" + yyyymmdd.substring(6);
    }

    /** "써스펜"과 "써스펜데이정"처럼 한쪽이 다른 쪽에 포함되면 같은 제품으로 본다. */
    static boolean isSameProduct(String a, String b) {
        String x = normalizeName(a);
        String y = normalizeName(b);
        return !x.isEmpty() && !y.isEmpty() && (x.contains(y) || y.contains(x));
    }

    static String normalizeName(String name) {
        return name == null ? "" : name.replaceAll("\\([^)]*\\)", "").replaceAll("[^0-9A-Za-z가-힣]", "").toLowerCase();
    }

    private Cabinet collectCabinet(String loginId) {
        Map<Long, List<Owner>> owners = new HashMap<>();
        Set<Long> medicineIds = new HashSet<>();
        if (loginId == null || loginId.isBlank()) {
            return new Cabinet(owners, medicineIds);
        }
        Optional<User> userOpt = userRepository.findByLoginId(loginId);
        if (userOpt.isEmpty()) {
            return new Cabinet(owners, medicineIds);
        }
        List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(userOpt.get().getId());

        List<Long> registeredMedicineIds = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.MEDICINE)
                .map(UserMedicationReg::getItemId)
                .distinct()
                .toList();
        medicineIds.addAll(registeredMedicineIds);
        if (!registeredMedicineIds.isEmpty()) {
            for (MedicineMaster medicine : medicineMasterRepository.findByIdIn(registeredMedicineIds)) {
                for (Long ingredientId : ingredientIds(medicine)) {
                    owners.computeIfAbsent(ingredientId, key -> new ArrayList<>())
                            .add(new Owner(medicine.getId(), medicine.getMedicineName()));
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
                    owners.computeIfAbsent(si.getIngredientMaster().getId(), key -> new ArrayList<>())
                            .add(new Owner(null, supplement.getSupplementName()));
                }
            }
        }
        return new Cabinet(owners, medicineIds);
    }
}
