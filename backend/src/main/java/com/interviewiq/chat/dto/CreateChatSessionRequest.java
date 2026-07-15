package com.interviewiq.chat.dto;

import jakarta.validation.constraints.NotNull;

public record CreateChatSessionRequest(@NotNull Long reportId) {
}

