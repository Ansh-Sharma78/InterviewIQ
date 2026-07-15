package com.interviewiq.chat.dto;

import com.interviewiq.chat.entity.ChatMessageRole;
import java.time.Instant;

public record ChatMessageResponse(Long id, ChatMessageRole role, String content, Instant createdAt) {
}

