package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPrescriptionOcrItemDto {
    private String medicineName;
    private String rawText;
    private String dosage;
    private String frequency;
    private String mealTiming;
    private String days;
    private double confidence;
}
