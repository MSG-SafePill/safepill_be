package com.meta.safepill_be.chatbot.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatSessionResponseDto {
    private Long sessionId;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;
}
