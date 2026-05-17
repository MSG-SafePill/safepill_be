package com.meta.safepill_be.vision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VisionMedicineCandidateDto {
    private Long medicineId;
    private String itemSeq;
    private String medicineName;
    private String manufacturer;
    private String imageUrl;
    private String efficacy;
    private String useMethod;
    private String precautions;
    private double confidence;
    private String matchedText;
    private List<VisionIngredientDto> ingredients;
    private List<VisionInteractionWarningDto> interactionWarnings;
}
