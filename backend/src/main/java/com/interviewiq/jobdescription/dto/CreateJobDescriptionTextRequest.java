package com.interviewiq.jobdescription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobDescriptionTextRequest(
        @Size(max = 160) String companyName,
        @Size(max = 160) String roleTitle,
        @NotBlank @Size(max = 50000) String rawText
) {
}

