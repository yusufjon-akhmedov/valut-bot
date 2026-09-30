--liquibase formatted sql
--changeset claude:001-init-schema

CREATE TABLE users (
    telegram_id BIGINT PRIMARY KEY,
    username VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE card_categories (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE cards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_telegram_id BIGINT NOT NULL REFERENCES users(telegram_id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES card_categories(id),
    card_number_encrypted BYTEA NOT NULL,
    card_number_hash VARCHAR(64) NOT NULL,
    last4 VARCHAR(4) NOT NULL,
    expiry_month INTEGER NOT NULL,
    expiry_year INTEGER NOT NULL,
    holder_name VARCHAR(255),
    label VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_cards_owner ON cards(owner_telegram_id);
CREATE INDEX idx_cards_category ON cards(category_id);
CREATE INDEX idx_cards_hash ON cards(card_number_hash);

--rollback DROP INDEX IF EXISTS idx_cards_hash;
--rollback DROP INDEX IF EXISTS idx_cards_category;
--rollback DROP INDEX IF EXISTS idx_cards_owner;
--rollback DROP TABLE IF EXISTS cards;
--rollback DROP TABLE IF EXISTS card_categories;
--rollback DROP TABLE IF EXISTS users;
