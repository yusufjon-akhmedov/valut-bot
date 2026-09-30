# Implementation Plan: Telegram Card Vault Bot

## Milestones
1. **Deps + Config + Compose** — Add all dependencies (JPA, PostgreSQL, Telegram, Liquibase, Crypto, Validation, etc.), set up compose.yaml with PostgreSQL, wire up application.yaml with profiles and security config.
2. **Liquibase Schema** — Create schema with users, card_categories, cards tables; seed categories; write rollback scripts.
3. **Domain + Crypto + Service Layer** — Entity models, AES-GCM encryption/decryption, CardService, UserService, card validation (Luhn, category detection, masking).
4. **Telegram Layer** — Bot registration, command handlers (/start, /help, /add, /cards, /delete, /cancel), dialog state management, inline mode, keyboards, messages resource.
5. **Tests** — Unit tests for crypto, validation, category detection; integration tests with Testcontainers; handler tests with mocked Telegram client.
6. **Docs + Final Checks** — README.md, .env.example, DECISIONS.md, PROGRESS.md; verify clean build, boot against Postgres, confirm health endpoint and Liquibase ran.

## Key Technical Decisions
- Encryption key must be loaded from env on startup; app fails if missing.
- Dialog state persisted in DB (ConversationState table), not in-memory.
- Telegram results filtered server-side to user's own cards only.
- Categories seeded via Liquibase changeset (VISA, MASTERCARD, UZCARD, HUMO).
- Use official org.telegram:telegrambots library, NOT a starter.
- Testcontainers for integration tests (detect Docker availability gracefully).
- All user-facing text centralized in Messages class + properties file.

## Critical Security Checklist
- [ ] Encryption key validation on startup
- [ ] No card numbers in logs (test this)
- [ ] Last4 + hash stored alongside ciphertext
- [ ] Access control via allowed-user-ids
- [ ] .env.example + .env gitignored
- [ ] No CVV/CVC fields ever added
- [ ] Pre-commit scan for secrets

## Testing Coverage
- [ ] Luhn validation (valid/invalid cards)
- [ ] Category detection (Visa, Mastercard, Uzcard, Humo)
- [ ] Card masking (show only last 4)
- [ ] AES-GCM encrypt/decrypt round-trip + tamper detection
- [ ] Inline query returns only user's own cards
- [ ] /add dialog happy path + cancel at each step
- [ ] /cards listing grouped by category
- [ ] Access control allowlist
- [ ] Boot + health check + Liquibase applied
