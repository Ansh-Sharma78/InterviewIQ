package com.interviewiq.ai.client;

public record ReportAiResult(
        String payloadJson,
        int atsMatchScore,
        int interviewReadinessScore,
        int promptTokens,
        int completionTokens,
        String model
) {
}

