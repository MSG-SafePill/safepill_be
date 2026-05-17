package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiPrescriptionOcrResponseDto {
    private String requestId;
    private String status;
    private List<AiPrescriptionOcrItemDto> items;
    private List<AiOcrCandidateDto> rawCandidates;
}
