package com.interviewiq.common.file;

import com.interviewiq.common.exception.DomainException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;

public final class FileChecks {
    public static final long MAX_PDF_BYTES = 5L * 1024L * 1024L;

    private FileChecks() {
    }

    public static void requirePdf(byte[] content) {
        if (content.length > MAX_PDF_BYTES) {
            throw new DomainException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "PDF uploads must be 5MB or smaller");
        }
        if (content.length < 5 || !new String(content, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
            throw new DomainException(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE", "Only valid PDF files are allowed");
        }
    }

    public static String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}

