package com.interviewiq.analysis.dto;

import com.interviewiq.analysis.entity.ReportStatus;
import java.time.Instant;

public record ReportSummaryResponse(
        Long id,
        Long resumeId,
        Long jobDescriptionId,
        ReportStatus status,
        Integer atsMatchScore,
        Integer interviewReadinessScore,
        Instant createdAt
) {
}

