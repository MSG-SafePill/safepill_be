package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiPillClassificationResponseDto {
    private boolean success;
    private List<AiPillClassificationCandidateDto> candidates;
}
