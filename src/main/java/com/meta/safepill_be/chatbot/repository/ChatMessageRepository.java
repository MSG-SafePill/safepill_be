package com.meta.safepill_be.chatbot.repository;

import com.meta.safepill_be.chatbot.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatSession_IdOrderByCreatedAtAsc(Long sessionId);
}