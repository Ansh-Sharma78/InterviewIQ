package com.interviewiq.chat.entity;

import com.interviewiq.analysis.entity.Report;
import com.interviewiq.auth.entity.User;
import com.interviewiq.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "chat_sessions")
public class ChatSession extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String title;

    private Instant deletedAt;

    protected ChatSession() {
    }

    public ChatSession(User user, Report report, String title) {
        this.user = user;
        this.report = report;
        this.title = title;
    }

    public void rename(String title) {
        this.title = title;
    }

    public void softDelete() {
        deletedAt = Instant.now();
    }

    public Report getReport() {
        return report;
    }

    public String getTitle() {
        return title;
    }
}

