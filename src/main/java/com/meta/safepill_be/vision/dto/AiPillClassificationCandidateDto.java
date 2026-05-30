package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPillClassificationCandidateDto {
    private String classLabel;
    private String medicineName;
    private String manufacturer;
    private String mark;
    private String shape;
    private String color;
    private double score;
    private String reason;
}
