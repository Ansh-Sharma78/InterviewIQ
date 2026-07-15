CREATE TABLE resumes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    checksum CHAR(64) NOT NULL,
    parsed_text LONGTEXT NULL,
    parse_status VARCHAR(30) NOT NULL,
    deleted_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_resumes_user_deleted_created ON resumes(user_id, deleted_at, created_at);
CREATE INDEX idx_resumes_checksum ON resumes(checksum);

CREATE TABLE job_descriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(160) NULL,
    role_title VARCHAR(160) NULL,
    source_type VARCHAR(30) NOT NULL,
    original_filename VARCHAR(255) NULL,
    storage_key VARCHAR(500) NULL,
    file_size_bytes BIGINT NULL,
    mime_type VARCHAR(120) NULL,
    checksum CHAR(64) NULL,
    raw_text LONGTEXT NOT NULL,
    parse_status VARCHAR(30) NOT NULL,
    deleted_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_job_descriptions_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_job_descriptions_user_deleted_created ON job_descriptions(user_id, deleted_at, created_at);
CREATE INDEX idx_job_descriptions_checksum ON job_descriptions(checksum);

