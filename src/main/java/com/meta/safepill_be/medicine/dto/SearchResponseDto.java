package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponseDto {
    private List<MedicineSimpleDto> medicines;
    private List<SupplementSimpleDto> supplements;
    private int page;
    private int size;
    private boolean hasNext;
    private long totalElements;
}
