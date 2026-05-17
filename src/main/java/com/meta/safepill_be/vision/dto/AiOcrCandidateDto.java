package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiOcrCandidateDto {
    private String text;
    private String normalizedText;
    private double confidence;
    private int regionIndex;
}
