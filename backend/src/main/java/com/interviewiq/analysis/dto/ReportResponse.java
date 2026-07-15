package com.interviewiq.analysis.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.interviewiq.analysis.entity.ReportStatus;
import java.time.Instant;

public record ReportResponse(
        Long id,
        Long resumeId,
        Long jobDescriptionId,
        ReportStatus status,
        Integer atsMatchScore,
        Integer interviewReadinessScore,
        String failureReason,
        JsonNode payload,
        Instant createdAt
) {
}

