package com.interviewiq.jobdescription.entity;

import com.interviewiq.auth.entity.User;
import com.interviewiq.common.entity.BaseEntity;
import com.interviewiq.resume.entity.ParseStatus;
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
@Table(name = "job_descriptions")
public class JobDescription extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String companyName;

    private String roleTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobDescriptionSourceType sourceType;

    private String originalFilename;

    private String storageKey;

    private Long fileSizeBytes;

    private String mimeType;

    @Column(length = 64, columnDefinition = "CHAR(64)")
    private String checksum;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String rawText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParseStatus parseStatus;

    private Instant deletedAt;

    protected JobDescription() {
    }

    public JobDescription(User user, String companyName, String roleTitle, JobDescriptionSourceType sourceType, String originalFilename, String storageKey, Long fileSizeBytes, String mimeType, String checksum, String rawText, ParseStatus parseStatus) {
        this.user = user;
        this.companyName = companyName;
        this.roleTitle = roleTitle;
        this.sourceType = sourceType;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.checksum = checksum;
        this.rawText = rawText;
        this.parseStatus = parseStatus;
    }

    public void softDelete() {
        deletedAt = Instant.now();
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public JobDescriptionSourceType getSourceType() {
        return sourceType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getRawText() {
        return rawText;
    }

    public ParseStatus getParseStatus() {
        return parseStatus;
    }
}
