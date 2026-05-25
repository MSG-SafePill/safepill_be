package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.MedicationMatchCandidateDto;
import com.meta.safepill_be.medicine.dto.MedicationMatchRequestDto;
import com.meta.safepill_be.medicine.dto.MedicationMatchResultDto;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MedicationMatchService {
    private final MedicineMasterRepository medicineRepository;
    private final SupplementMasterRepository supplementRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MedicationMatchResultDto> match(String loginId, MedicationMatchRequestDto requestDto) {
        if (requestDto.getKeywords() == null || requestDto.getKeywords().isEmpty()) {
            return List.of();
        }

        RegisteredIds registeredIds = loginId == null ? new RegisteredIds(Set.of(), Set.of()) : resolveRegisteredIds(loginId);
        int topK = requestDto.getTopK() == null ? 5 : Math.max(1, Math.min(requestDto.getTopK(), 20));

        return requestDto.getKeywords().stream()
                .map(keyword -> matchKeyword(keyword, topK, registeredIds))
                .toList();
    }

    private MedicationMatchResultDto matchKeyword(String rawKeyword, int topK, RegisteredIds registeredIds) {
        String keyword = rawKeyword == null ? "" : rawKeyword.trim();
        if (keyword.isEmpty()) {
            return MedicationMatchResultDto.builder()
                    .keyword(rawKeyword)
                    .candidates(List.of())
                    .build();
        }

        List<MedicationMatchCandidateDto> medicines = medicineRepository.findTop5ByMedicineNameContaining(keyword).stream()
                .map(medicine -> MedicationMatchCandidateDto.builder()
                        .itemType(ItemType.MEDICINE)
                        .itemId(medicine.getId())
                        .itemName(medicine.getMedicineName())
                        .manufacturer(medicine.getMedicineManufacturer())
                        .score(score(keyword, medicine.getMedicineName()))
                        .registered(registeredIds.medicineIds().contains(medicine.getId()))
                        .build())
                .toList();

        List<MedicationMatchCandidateDto> supplements = supplementRepository.findTop5BySupplementNameContaining(keyword).stream()
                .map(supplement -> MedicationMatchCandidateDto.builder()
                        .itemType(ItemType.SUPPLEMENT)
                        .itemId(supplement.getId())
                        .itemName(supplement.getSupplementName())
                        .manufacturer(supplement.getSupplementManufacturer())
                        .score(score(keyword, supplement.getSupplementName()))
                        .registered(registeredIds.supplementIds().contains(supplement.getId()))
                        .build())
                .toList();

        return MedicationMatchResultDto.builder()
                .keyword(keyword)
                .candidates(Stream.concat(medicines.stream(), supplements.stream())
                        .sorted(Comparator.comparing(MedicationMatchCandidateDto::getScore).reversed())
                        .limit(topK)
                        .toList())
                .build();
    }

    private double score(String keyword, String candidateName) {
        String normalizedKeyword = normalize(keyword);
        String normalizedCandidate = normalize(candidateName);
        if (normalizedCandidate.equals(normalizedKeyword)) {
            return 1.0;
        }
        if (normalizedCandidate.startsWith(normalizedKeyword)) {
            return 0.9;
        }
        if (normalizedCandidate.contains(normalizedKeyword)) {
            return 0.75;
        }
        return 0.5;
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
    }

    private RegisteredIds resolveRegisteredIds(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<UserMedicationReg> registrations = userMedicationRegRepository.findByUserId(user.getId());
        Set<Long> medicineIds = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.MEDICINE)
                .map(UserMedicationReg::getItemId)
                .collect(Collectors.toSet());
        Set<Long> supplementIds = registrations.stream()
                .filter(reg -> reg.getItem_type() == ItemType.SUPPLEMENT)
                .map(UserMedicationReg::getItemId)
                .collect(Collectors.toSet());
        return new RegisteredIds(medicineIds, supplementIds);
    }

    private record RegisteredIds(Set<Long> medicineIds, Set<Long> supplementIds) {
    }
}
