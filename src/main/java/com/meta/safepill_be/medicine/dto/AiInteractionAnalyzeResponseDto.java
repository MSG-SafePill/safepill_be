package com.meta.safepill_be.medicine.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiInteractionAnalyzeResponseDto {
    private String requestId;
    private String status;
    private String riskLevel;
    private String summary;
    private List<AiInteractionWarningDto> warnings;
    private List<String> recommendations;
    private List<AiInteractionEvidenceDto> evidence;
    private String disclaimer;
}
