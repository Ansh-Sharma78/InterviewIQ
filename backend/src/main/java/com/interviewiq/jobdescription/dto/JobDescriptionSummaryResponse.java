package com.interviewiq.jobdescription.dto;

import com.interviewiq.jobdescription.entity.JobDescriptionSourceType;
import com.interviewiq.resume.entity.ParseStatus;
import java.time.Instant;

public record JobDescriptionSummaryResponse(
        Long id,
        String companyName,
        String roleTitle,
        JobDescriptionSourceType sourceType,
        ParseStatus parseStatus,
        String textPreview,
        Instant createdAt
) {
}

