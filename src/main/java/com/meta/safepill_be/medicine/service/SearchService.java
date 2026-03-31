package com.meta.safepill_be.medicine.service;

import com.meta.safepill_be.medicine.dto.MedicineSimpleDto;
import com.meta.safepill_be.medicine.dto.SearchResponseDto;
import com.meta.safepill_be.medicine.dto.SupplementSimpleDto;
import com.meta.safepill_be.medicine.repository.MedicineMasterRepository;
import com.meta.safepill_be.medicine.repository.SupplementMasterRepository;
import lombok.RequiredArgsConstructor;
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

    public SearchResponseDto searchMedicineAndSupplement(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchResponseDto.builder()
                    .medicines(List.of())
                    .supplements(List.of())
                    .build();
        }

        String searchWord = keyword.trim();

        List<MedicineSimpleDto> medicineDtos = medicineMasterRepository.findByMedicineNameContaining(searchWord)
                .stream()
                .map(MedicineSimpleDto::from)
                .collect(Collectors.toList());

        List<SupplementSimpleDto> supplementDtos = supplementMasterRepository.findBySupplementNameContaining(searchWord)
                .stream()
                .map(SupplementSimpleDto::from)
                .collect(Collectors.toList());

        return SearchResponseDto.builder()
                .medicines(medicineDtos)
                .supplements(supplementDtos)
                .build();
    }
}