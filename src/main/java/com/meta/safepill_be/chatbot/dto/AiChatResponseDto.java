package com.meta.safepill_be.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AiChatResponseDto {
    private String requestId;
    private String status;
    private String answer;
    private List<String> referencedPills;
}
