package com.interviewiq.resume.dto;

import com.interviewiq.resume.entity.ParseStatus;
import java.time.Instant;

public record ResumeResponse(
        Long id,
        String originalFilename,
        long fileSizeBytes,
        String mimeType,
        String checksum,
        ParseStatus parseStatus,
        String parsedText,
        Instant createdAt
) {
}

