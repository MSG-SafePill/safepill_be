package com.meta.safepill_be.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiChatRequestDto {
    private String question;
    private List<String> identifiedPills;
    private String imagePath;
}
