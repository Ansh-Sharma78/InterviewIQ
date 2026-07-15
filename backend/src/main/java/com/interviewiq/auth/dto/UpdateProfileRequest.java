package com.interviewiq.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 120) String targetRole,
        @Size(max = 80) String experienceLevel,
        @Size(max = 500) String targetCompanies
) {
}

