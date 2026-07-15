package com.interviewiq.auth.dto;

public record AuthResponse(String accessToken, UserProfileResponse user) {
}

