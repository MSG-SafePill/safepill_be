package com.meta.safepill_be.medicine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InteractionAnalyzeRequestDto {
    private List<Long> medicineIds;
}