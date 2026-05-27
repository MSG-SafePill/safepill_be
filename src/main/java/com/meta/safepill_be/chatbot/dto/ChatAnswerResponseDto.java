package com.meta.safepill_be.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatAnswerResponseDto {
    private ChatMessageResponseDto userMessage;
    private ChatMessageResponseDto assistantMessage;
    private List<String> referencedPills;
    private boolean fallback;
}
