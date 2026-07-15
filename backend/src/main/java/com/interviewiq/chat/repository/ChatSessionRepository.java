package com.interviewiq.chat.repository;

import com.interviewiq.chat.entity.ChatSession;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Page<ChatSession> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Optional<ChatSession> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}

