package com.interviewiq.common.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String correlationId,
        List<FieldViolation> fieldErrors
) {
    public record FieldViolation(String field, String message) {
    }
}

