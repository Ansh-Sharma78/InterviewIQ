package com.interviewiq.ai.usage;

import com.interviewiq.auth.entity.User;
import com.interviewiq.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_usage_logs")
public class AiUsageLog extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String feature;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private int promptTokens;

    @Column(nullable = false)
    private int completionTokens;

    @Column(nullable = false)
    private int totalTokens;

    @Column(nullable = false)
    private boolean success;

    private String errorCode;

    protected AiUsageLog() {
    }

    public AiUsageLog(User user, String feature, String provider, String model, int promptTokens, int completionTokens, boolean success, String errorCode) {
        this.user = user;
        this.feature = feature;
        this.provider = provider;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        this.success = success;
        this.errorCode = errorCode;
    }
}

