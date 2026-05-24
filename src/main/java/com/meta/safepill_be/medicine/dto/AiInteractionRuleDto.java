package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiInteractionRuleDto {
    private String itemNameA;
    private String itemNameB;
    private String ingredientNameA;
    private String ingredientNameB;
    private String riskLevel;
    private String description;
}
