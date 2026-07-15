package com.interviewiq.ai.client;

public record ReportAiRequest(
        String resumeText,
        String jobDescriptionText,
        String companyName,
        String roleTitle
) {
}

