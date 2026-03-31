package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.SearchResponseDto;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;

    public SearchResponseDto searchMedicineAndSupplement(String keyword) {

        // 1. 키워드가 없거나 공백이면 빈 상자 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchResponseDto.builder()
                    .medicines(List.of())
                    .supplements(List.of())
                    .build();
        }

        // 2. DB에서 키워드가 포함된 약과 영양제를 각각 긁어옵니다.
        List<MedicineMaster> medicines = medicineMasterRepository.findByMedicineNameContaining(keyword.trim());
        List<SupplementMaster> supplements = supplementMasterRepository.findBySupplementNameContaining(keyword.trim());

        // 3. 예쁜 통합 상자에 담아서 반환!
        return SearchResponseDto.builder()
                .medicines(medicines)
                .supplements(supplements)
                .build();
    }
}
