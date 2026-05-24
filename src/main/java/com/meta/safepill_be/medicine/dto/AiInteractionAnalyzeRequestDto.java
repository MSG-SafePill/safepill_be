package com.meta.safepill_be.medicine.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AiInteractionAnalyzeRequestDto {
    private List<AiInteractionItemDto> items;
    private List<AiInteractionRuleDto> interactionRules;
    private Map<String, Object> userProfile;
}
