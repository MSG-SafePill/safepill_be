package com.meta.safepill_be.medicine.dto;

import com.meta.safepill_be.cabinet.domain.ItemType;
import com.meta.safepill_be.medicine.domain.RiskLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InteractionAnalyzeResponseDto {
    private String itemNameA;
    private String itemNameB;
    private ItemType itemTypeA;
    private ItemType itemTypeB;
    private String medicineNameA;
    private String medicineNameB;
    private String ingredientNameA;
    private String ingredientNameB;
    private RiskLevel riskLevel;
    private String description;
}
