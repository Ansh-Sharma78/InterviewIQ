package com.interviewiq.ai.client;

import java.util.List;

public record ChatAiRequest(
        String reportPayloadJson,
        String resumeText,
        String jobDescriptionText,
        List<ChatTurn> recentTurns,
        String userMessage
) {
    public record ChatTurn(String role, String content) {
    }
}

