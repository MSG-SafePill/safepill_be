package com.meta.safepill_be.medicine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MedicationMatchRequestDto {
    private List<String> keywords;
    private Integer topK;
}
