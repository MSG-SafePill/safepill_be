package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MedicineAlternativeDto {
    public static final String SAME_INGREDIENT = "SAME_INGREDIENT";
    public static final String PARTIAL_INGREDIENT = "PARTIAL_INGREDIENT";
    public static final String SAME_EFFICACY_GROUP = "SAME_EFFICACY_GROUP";
    public static final String AI_SUGGESTED = "AI_SUGGESTED";

    private Long id;
    private String medicineName;
    private String manufacturer;
    private String matchType;
    private List<String> sharedIngredients;
    private List<String> ingredientSummaries;
    private boolean dosageDiffers;
    private boolean hasCabinetConflict;
    private List<String> conflictReasons;
    @Builder.Default
    private boolean isAiSuggested = false;
    @Builder.Default
    private boolean isVerifiedInDb = false;
    private String aiReason;
}
