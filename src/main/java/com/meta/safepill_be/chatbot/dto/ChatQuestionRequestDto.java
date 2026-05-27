package com.meta.safepill_be.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatQuestionRequestDto {
    private String question;
    private Boolean useMyCabinet;
    private List<String> identifiedPills;
}
