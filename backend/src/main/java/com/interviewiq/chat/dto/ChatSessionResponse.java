package com.interviewiq.chat.dto;

import java.time.Instant;

public record ChatSessionResponse(Long id, Long reportId, String title, Instant createdAt) {
}

