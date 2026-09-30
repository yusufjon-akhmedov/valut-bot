--liquibase formatted sql
--changeset claude:003-conversation-state

CREATE TABLE conversation_state (
    id SERIAL PRIMARY KEY,
    user_telegram_id BIGINT NOT NULL REFERENCES users(telegram_id) ON DELETE CASCADE,
    state VARCHAR(50) NOT NULL,
    data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_conversation_state_user ON conversation_state(user_telegram_id);

--rollback DROP INDEX IF EXISTS idx_conversation_state_user;
--rollback DROP TABLE IF EXISTS conversation_state;
