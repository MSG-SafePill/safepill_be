package com.meta.safepill_be.chatbot.dto;

import com.meta.safepill_be.chatbot.domain.SenderRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponseDto {
    private Long messageId;
    private Long sessionId;
    private SenderRole senderRole;
    private String contents;
    private LocalDateTime createdAt;
}
