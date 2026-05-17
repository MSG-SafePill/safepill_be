package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPillDetectionDto {
    private String pillName;
    private double confidence;
}
