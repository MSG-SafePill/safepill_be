package com.meta.safepill_be.vision.dto;

import com.meta.safepill_be.medicine.domain.RiskLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisionInteractionWarningDto {
    private String sourceIngredient;
    private String targetIngredient;
    private RiskLevel riskLevel;
    private String description;
}
