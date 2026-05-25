package com.meta.safepill_be.chatbot.controller;

import com.meta.safepill_be.chatbot.dto.ChatAnswerResponseDto;
import com.meta.safepill_be.chatbot.dto.ChatMessageResponseDto;
import com.meta.safepill_be.chatbot.dto.ChatQuestionRequestDto;
import com.meta.safepill_be.chatbot.dto.ChatSessionResponseDto;
import com.meta.safepill_be.chatbot.service.ChatService;
import com.meta.safepill_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    @PostMapping("/sessions")
    public ResponseEntity<ChatSessionResponseDto> createSession(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(chatService.createSession(extractLoginId(token)));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionResponseDto>> getSessions(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(chatService.getSessions(extractLoginId(token)));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(chatService.getMessages(extractLoginId(token), sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ChatAnswerResponseDto> ask(
            @RequestHeader("Authorization") String token,
            @PathVariable Long sessionId,
            @RequestBody ChatQuestionRequestDto requestDto) {
        return ResponseEntity.ok(chatService.ask(extractLoginId(token), sessionId, requestDto));
    }

    private String extractLoginId(String token) {
        return jwtUtil.getLoginIdFromToken(token.replace("Bearer ", ""));
    }
}
