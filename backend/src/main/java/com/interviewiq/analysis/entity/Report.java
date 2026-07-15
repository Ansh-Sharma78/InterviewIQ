package com.interviewiq.analysis.entity;

import com.interviewiq.auth.entity.User;
import com.interviewiq.common.entity.BaseEntity;
import com.interviewiq.jobdescription.entity.JobDescription;
import com.interviewiq.resume.entity.Resume;
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
@Table(name = "reports")
public class Report extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(columnDefinition = "LONGTEXT")
    private String payloadJson;

    private Integer atsMatchScore;

    private Integer interviewReadinessScore;

    private String failureReason;

    private Instant deletedAt;

    protected Report() {
    }

    public Report(User user, Resume resume, JobDescription jobDescription) {
        this.user = user;
        this.resume = resume;
        this.jobDescription = jobDescription;
    }

    public void markProcessing() {
        status = ReportStatus.PROCESSING;
        failureReason = null;
    }

    public void markCompleted(String payloadJson, int atsMatchScore, int interviewReadinessScore) {
        status = ReportStatus.COMPLETED;
        this.payloadJson = payloadJson;
        this.atsMatchScore = atsMatchScore;
        this.interviewReadinessScore = interviewReadinessScore;
        failureReason = null;
    }

    public void markFailed(String reason) {
        status = ReportStatus.FAILED;
        failureReason = reason;
    }

    public void softDelete() {
        deletedAt = Instant.now();
    }

    public Resume getResume() {
        return resume;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public Integer getAtsMatchScore() {
        return atsMatchScore;
    }

    public Integer getInterviewReadinessScore() {
        return interviewReadinessScore;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
