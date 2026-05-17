package com.meta.safepill_be.vision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PrescriptionOcrItemResponseDto {
    private String rawText;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String mealTiming;
    private String days;
    private double confidence;
    private Long matchedMedicineId;
    private String matchedMedicineName;
    private List<VisionMedicineCandidateDto> candidates;
}
