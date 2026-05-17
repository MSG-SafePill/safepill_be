package com.meta.safepill_be.vision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VisionIdentifyResponseDto {
    private String requestId;
    private String status;
    private List<VisionMedicineCandidateDto> candidates;
}
