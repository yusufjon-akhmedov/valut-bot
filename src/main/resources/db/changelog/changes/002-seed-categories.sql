--liquibase formatted sql
--changeset claude:002-seed-categories

INSERT INTO card_categories (code, name) VALUES
    ('VISA', 'Visa'),
    ('MASTERCARD', 'Mastercard'),
    ('UZCARD', 'Uzcard'),
    ('HUMO', 'Humo');

--rollback DELETE FROM card_categories WHERE code IN ('VISA', 'MASTERCARD', 'UZCARD', 'HUMO');
