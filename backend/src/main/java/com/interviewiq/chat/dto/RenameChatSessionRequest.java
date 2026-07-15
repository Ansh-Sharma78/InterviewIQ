package com.interviewiq.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameChatSessionRequest(@NotBlank @Size(max = 160) String title) {
}

