package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.cabinet.domain.UserMedicationReg;
import com.meta.safepill_be.cabinet.repository.UserMedicationRegRepository;
import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.MedicineSimpleDto;
import com.meta.safepill_be.medicine.dto.SearchResponseDto;
import com.meta.safepill_be.medicine.dto.SupplementSimpleDto;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import com.meta.safepill_be.user.domain.User;
import com.meta.safepill_be.user.repository.UserRepository;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;
    private final UserMedicationRegRepository userMedicationRegRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public SearchResponseDto searchMedicineAndSupplement(String keyword, Pageable pageable) {
        return searchMedicineAndSupplement(keyword, pageable, null);
    }

    public SearchResponseDto searchMedicineAndSupplement(String keyword, Pageable pageable, String authorizationHeader) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchResponseDto.builder()
                    .medicines(List.of())
                    .supplements(List.of())
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .hasNext(false)
                    .totalElements(0)
                    .build();
        }

        RegisteredItemIds registeredItemIds = resolveRegisteredItemIds(authorizationHeader);
        String searchWord = keyword.trim();

        Page<MedicineMaster> medicinePage = medicineMasterRepository.findByMedicineNameContaining(searchWord, pageable);
        List<MedicineSimpleDto> medicineDtos = medicinePage.getContent() // .getContent()로 해당 페이지의 데이터만 쏙 뽑아냅니다.
                .stream()
                .map(medicine -> MedicineSimpleDto.from(medicine, registeredItemIds.medicineIds().contains(medicine.getId())))
                .collect(Collectors.toList());

        Page<SupplementMaster> supplementPage = supplementMasterRepository.findBySupplementNameContaining(searchWord, pageable);
        List<SupplementSimpleDto> supplementDtos = supplementPage.getContent()
                .stream()
                .map(supplement -> SupplementSimpleDto.from(supplement, registeredItemIds.supplementIds().contains(supplement.getId())))
                .collect(Collectors.toList());

        return SearchResponseDto.builder()
                .medicines(medicineDtos)
                .supplements(supplementDtos)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .hasNext(medicinePage.hasNext() || supplementPage.hasNext())
                .totalElements(medicinePage.getTotalElements() + supplementPage.getTotalElements())
                .build();
    }

    private RegisteredItemIds resolveRegisteredItemIds(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return new RegisteredItemIds(Set.of(), Set.of());
        }

        String token = authorizationHeader.replace("Bearer ", "");
        String loginId = jwtUtil.getLoginIdFromToken(token);
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

        return new RegisteredItemIds(medicineIds, supplementIds);
    }

    private record RegisteredItemIds(Set<Long> medicineIds, Set<Long> supplementIds) {
    }
}
