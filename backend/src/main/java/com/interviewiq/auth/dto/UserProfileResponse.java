package com.interviewiq.auth.dto;

import com.interviewiq.auth.entity.Role;
import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        String targetRole,
        String experienceLevel,
        String targetCompanies,
        Role role,
        Instant createdAt
) {
}

