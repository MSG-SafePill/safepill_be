package com.meta.safepill_be.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AiChatRequestDto {
    private String question;
    private List<String> identifiedPills;
    private List<Map<String, Object>> contextItems;
    private Map<String, Object> userProfile;
    private String imagePath;
}
