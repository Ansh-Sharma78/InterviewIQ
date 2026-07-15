package com.interviewiq.jobdescription.dto;

import com.interviewiq.jobdescription.entity.JobDescriptionSourceType;
import com.interviewiq.resume.entity.ParseStatus;
import java.time.Instant;

public record JobDescriptionResponse(
        Long id,
        String companyName,
        String roleTitle,
        JobDescriptionSourceType sourceType,
        String originalFilename,
        Long fileSizeBytes,
        String mimeType,
        String checksum,
        ParseStatus parseStatus,
        String rawText,
        Instant createdAt
) {
}

