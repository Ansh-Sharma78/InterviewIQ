package com.interviewiq.ai.client;

public interface AiClient {
    ReportAiResult generateReport(ReportAiRequest request);

    ChatAiResult generateChatReply(ChatAiRequest request);

    String provider();
}

