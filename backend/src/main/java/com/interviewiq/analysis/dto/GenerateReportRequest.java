package com.interviewiq.analysis.dto;

import jakarta.validation.constraints.NotNull;

public record GenerateReportRequest(
        @NotNull Long resumeId,
        @NotNull Long jobDescriptionId
) {
}

