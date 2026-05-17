package com.meta.safepill_be.vision.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiIdentifyResponseDto {
    private String requestId;
    private String status;
    private List<AiPillDetectionDto> detections;
    private List<AiOcrCandidateDto> ocrCandidates;
    private List<AiIdentifiedPillDto> identifiedPills;
}
