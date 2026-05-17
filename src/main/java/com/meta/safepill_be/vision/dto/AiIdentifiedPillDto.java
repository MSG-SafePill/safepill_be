package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiIdentifiedPillDto {
    private String pillName;
    private double confidence;
    private double ocrScore;
    private double detectionScore;
    private String matchedText;
}
