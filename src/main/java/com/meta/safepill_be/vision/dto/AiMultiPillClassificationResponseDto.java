package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiMultiPillClassificationResponseDto {
    private boolean success;
    private int detectedCount;
    private List<AiDetectedPillClassificationDto> detectedPills;
}
