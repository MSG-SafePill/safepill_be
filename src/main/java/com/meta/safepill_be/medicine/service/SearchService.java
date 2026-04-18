package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.domain.MedicineMaster;
import com.meta.safepill_be.medicine.domain.SupplementMaster;
import com.meta.safepill_be.medicine.dto.MedicineSimpleDto;
import com.meta.safepill_be.medicine.dto.SearchResponseDto;
import com.meta.safepill_be.medicine.dto.SupplementSimpleDto;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final MedicineMasterRepository medicineMasterRepository;
    private final SupplementMasterRepository supplementMasterRepository;

    public SearchResponseDto searchMedicineAndSupplement(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchResponseDto.builder()
                    .medicines(List.of())
                    .supplements(List.of())
                    .build();
        }

        String searchWord = keyword.trim();

        Page<MedicineMaster> medicinePage = medicineMasterRepository.findByMedicineNameContaining(searchWord, pageable);
        List<MedicineSimpleDto> medicineDtos = medicinePage.getContent() // .getContent()로 해당 페이지의 데이터만 쏙 뽑아냅니다.
                .stream()
                .map(MedicineSimpleDto::from)
                .collect(Collectors.toList());

        Page<SupplementMaster> supplementPage = supplementMasterRepository.findBySupplementNameContaining(searchWord, pageable);
        List<SupplementSimpleDto> supplementDtos = supplementPage.getContent()
                .stream()
                .map(SupplementSimpleDto::from)
                .collect(Collectors.toList());

        return SearchResponseDto.builder()
                .medicines(medicineDtos)
                .supplements(supplementDtos)
                .build();
    }
}