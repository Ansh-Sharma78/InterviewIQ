CREATE TABLE reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_description_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    payload_json LONGTEXT NULL,
    ats_match_score INT NULL,
    interview_readiness_score INT NULL,
    failure_reason VARCHAR(1000) NULL,
    deleted_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_reports_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reports_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    CONSTRAINT fk_reports_job_description FOREIGN KEY (job_description_id) REFERENCES job_descriptions(id)
);

CREATE INDEX idx_reports_user_deleted_created ON reports(user_id, deleted_at, created_at);
CREATE INDEX idx_reports_user_scores ON reports(user_id, ats_match_score, interview_readiness_score);

CREATE TABLE ai_usage_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    feature VARCHAR(40) NOT NULL,
    provider VARCHAR(40) NOT NULL,
    model VARCHAR(120) NOT NULL,
    prompt_tokens INT NOT NULL DEFAULT 0,
    completion_tokens INT NOT NULL DEFAULT 0,
    total_tokens INT NOT NULL DEFAULT 0,
    success BOOLEAN NOT NULL,
    error_code VARCHAR(120) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_ai_usage_logs_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_ai_usage_logs_user_created ON ai_usage_logs(user_id, created_at);

