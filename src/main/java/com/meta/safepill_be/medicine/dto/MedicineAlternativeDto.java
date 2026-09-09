package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineAlternativeDto {
    private Long id;
    private String medicineName;
    private String manufacturer;
    private List<String> sharedIngredients;
    private boolean hasCabinetConflict;
    private List<String> conflictReasons;
    @Builder.Default
    private boolean isAiSuggested = false;
    @Builder.Default
    private boolean isVerifiedInDb = false;
    private String aiReason;
}
