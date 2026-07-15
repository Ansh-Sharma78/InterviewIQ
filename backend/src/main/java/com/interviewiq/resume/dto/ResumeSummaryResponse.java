package com.interviewiq.resume.dto;

import com.interviewiq.resume.entity.ParseStatus;
import java.time.Instant;

public record ResumeSummaryResponse(
        Long id,
        String originalFilename,
        long fileSizeBytes,
        ParseStatus parseStatus,
        String parsedTextPreview,
        Instant createdAt
) {
}

