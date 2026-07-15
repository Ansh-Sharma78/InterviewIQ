CREATE TABLE chat_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_chat_sessions_report FOREIGN KEY (report_id) REFERENCES reports(id)
);

CREATE INDEX idx_chat_sessions_user_deleted_created ON chat_sessions(user_id, deleted_at, created_at);

CREATE TABLE chat_messages (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chat_session_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (chat_session_id) REFERENCES chat_sessions(id)
);

CREATE INDEX idx_chat_messages_session ON chat_messages(chat_session_id);
