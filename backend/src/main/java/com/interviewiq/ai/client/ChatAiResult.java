package com.interviewiq.ai.client;

public record ChatAiResult(
        String content,
        int promptTokens,
        int completionTokens,
        String model
) {
}

