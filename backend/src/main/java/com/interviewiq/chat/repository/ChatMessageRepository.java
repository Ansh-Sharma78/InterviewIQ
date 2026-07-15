package com.interviewiq.chat.repository;

import com.interviewiq.chat.entity.ChatMessage;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByChatSessionIdOrderByCreatedAtAsc(Long sessionId, Pageable pageable);

    List<ChatMessage> findTop8ByChatSessionIdOrderByCreatedAtDesc(Long sessionId);

    long countByChatSessionId(Long sessionId);
}
