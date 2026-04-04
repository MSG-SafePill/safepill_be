package com.meta.safepill_be.medicine.dto;

import com.meta.safepill_be.medicine.domain.RiskLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InteractionAnalyzeResponseDto {
    private String medicineNameA;
    private String medicineNameB;
    private String ingredientNameA;
    private String ingredientNameB;
    private RiskLevel riskLevel;
    private String description;
}