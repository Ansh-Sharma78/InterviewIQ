package com.interviewiq.resume.entity;

import com.interviewiq.auth.entity.User;
import com.interviewiq.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "resumes")
public class Resume extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storageKey;

    @Column(nullable = false)
    private long fileSizeBytes;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false, length = 64, columnDefinition = "CHAR(64)")
    private String checksum;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "LONGTEXT")
    private String parsedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParseStatus parseStatus;

    private Instant deletedAt;

    protected Resume() {
    }

    public Resume(User user, String originalFilename, String storageKey, long fileSizeBytes, String mimeType, String checksum, String parsedText, ParseStatus parseStatus) {
        this.user = user;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.checksum = checksum;
        this.parsedText = parsedText;
        this.parseStatus = parseStatus;
    }

    public void softDelete() {
        deletedAt = Instant.now();
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getParsedText() {
        return parsedText;
    }

    public ParseStatus getParseStatus() {
        return parseStatus;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
}
