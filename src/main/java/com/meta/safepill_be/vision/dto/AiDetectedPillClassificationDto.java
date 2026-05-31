package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiDetectedPillClassificationDto {
    private int pillIndex;
    private List<AiPillClassificationCandidateDto> candidates;
}
