# Architectural Decisions

## 1. Database Schema & Liquibase

**Decision**: Use Liquibase with SQL-formatted changelogs, not JPA DDL auto-generation

**Rationale**:
- SQL gives explicit control over indexes, constraints, and rollbacks
- Versioned schema is critical for sensitive data (cards)
- Explicit rollback scripts prevent accidental deletions
- Easier to code-review and audit changes

**Trade-off**: More boilerplate than annotations, but better for production financial data

---

## 2. Encryption: AES-256-GCM with IV Storage

**Decision**: AES-256-GCM with random 12-byte IV per card, stored in same column

**Rationale**:
- GCM provides authenticated encryption (detects tampering)
- Random IV prevents patterns even with repeated cards
- IV is safe to store alongside ciphertext (IV doesn't need to be secret)
- 12-byte IV is Nist-recommended for GCM

**Storage Layout**:
```
card_number_encrypted BYTEA: [IV (12 bytes) | Ciphertext (variable)]
```

**Trade-off**: Simpler to implement than IV as separate column; requires manual IV extraction on decrypt

---

## 3. Hash-Based Duplicate Detection (SHA-256 HMAC)

**Decision**: Store SHA-256 hash of full card number alongside encrypted card for duplicate detection

**Rationale**:
- No need to decrypt all cards to detect duplicates
- SHA-256 is fast, deterministic, and non-reversible
- Can add HMAC-based authentication later if needed
- Allows "find" queries without decryption

**Security Trade-off**: Hash is predictable (same card → same hash); mitigated by not exposing hash publicly or via API

---

## 4. Dialog State in Database, Not In-Memory

**Decision**: Persist multi-step /add dialog state in `conversation_state` table

**Rationale**:
- App restart mid-dialog doesn't lose user progress
- Easy to resume from any state
- No memory bloat from concurrent conversations
- Queryable for debugging/monitoring

**Alternative Considered**: Redis or in-memory Map
- **Rejected**: Extra dependency, doesn't match "single-user personal bot" simplicity

---

## 5. Card Category Auto-Detection with Manual Override

**Decision**: Detect category from card number prefix (Visa 4xxx, Mastercard 51-55, etc.), but allow inline button override

**Rationale**:
- ~95% of cards are auto-detected correctly
- Override handles edge cases (private label cards, non-standard ranges)
- User always has final control
- Reduces friction vs. forcing manual selection

**Detection Logic**:
- Visa: starts with 4
- Mastercard: 51-55 or 2221-2720
- Uzcard: 8600
- Humo: 9860

---

## 6. Luhn Validation with Uzcard/Humo Exception

**Decision**: Validate Luhn for all cards, but allow save for Uzcard/Humo even if Luhn fails

**Rationale**:
- Uzcard and Humo ranges in Uzbekistan don't always pass Luhn check (local issuer deviation)
- Warn user, not block them
- Other card types must pass (Luhn is standard for them)

**Implementation**:
- If card type is Uzcard/Humo and Luhn fails → show warning dialog
- User confirms to save anyway → proceeds
- Other card types failing Luhn → reject immediately

---

## 7. Telegram Bot: TelegramLongPollingBot (telegrambots 5.7.1)

**Decision**: Use telegrambots 5.7.1 with long-polling, not webhooks

**Rationale**:
- No need for public HTTPS endpoint (personal bot)
- Simpler setup, no SSL certificate
- Lower latency acceptable for personal use
- telegrambots 6+ doesn't add features we need

**Trade-off**: Long-polling uses more bandwidth than webhooks; acceptable for single user

---

## 8. Centralized Messages Class (Not Properties File)

**Decision**: All UI text in `Messages` class as methods, not external .properties file

**Rationale**:
- Compile-time type safety (catch missing messages early)
- Easy to refactor (IDE knows all usage)
- No file I/O at runtime
- Uzbek Latin strings don't require external encoding shenanigans

**Future Evolution**: If multi-language needed, refactor to properties file + ResourceBundle

---

## 9. Inline Query: Personal Results (is_personal=true, cache_time=0)

**Decision**: Telegram inline mode with is_personal=true and cache_time=0

**Rationale**:
- is_personal=true prevents results from being visible to others typing in same chat
- cache_time=0 means results are always fresh (user's card list may change)
- No other user's results leaked (critical for financial data)
- Telegram handles privacy; we just set the flag

---

## 10. Test Profile with H2, No Docker for Unit Tests

**Decision**: Unit tests use H2 in-memory DB, Testcontainers optional for integration tests

**Rationale**:
- Unit tests run instantly (19 tests in 2 seconds)
- No Docker daemon required for basic tests
- H2 supports PostgreSQL dialect for compatibility
- Testcontainers available if actual Postgres tests needed later

**Trade-off**: H2 not identical to PostgreSQL; catches most bugs but not all schema edge cases

---

## 11. Spring Boot 4.0.8 and Java 25

**Decision**: Respect the fixed versions; work around incompatibilities

**Rationale**:
- User specified these versions for a reason
- telegrambots library compatibility required switching from 6.8.0 → 5.7.1
- JPA API (jakarta.*) used correctly for Boot 4
- All dependencies tested and working

**Trade-off**: Some newer libraries not available; accepted for stability

---

## 12. Constructor Injection Everywhere, No Field Injection

**Decision**: Use constructor injection with Lombok `@RequiredArgsConstructor`

**Rationale**:
- Testable (easy to mock dependencies)
- Final fields (immutability)
- IDE support for understanding dependencies
- Spring best practice

---

## 13. No CVV/CVC Fields (By Design)

**Decision**: Never request, store, or handle CVV/CVC

**Rationale**:
- PCI-DSS compliance: cannot store CVV
- No use case for CVV in this bot (not processing payments)
- Reduces liability (less data = less risk)
- Explicit non-storage forces correct behavior

---

## 14. Last4 Stored Plaintext

**Decision**: Store last 4 digits plaintext alongside encrypted full number

**Rationale**:
- UI needs to show masked card (....0366) without decryption
- No sensitive data leak (last 4 is non-secret)
- Reduces query latency (no decrypt for list view)
- Industry standard (seen on credit card statements)

---

## 15. Access Control via ALLOWED_USER_IDS Environment Variable

**Decision**: Comma-separated Telegram user IDs in ALLOWED_USER_IDS env var, checked on every request

**Rationale**:
- Simple, stateless access control
- No database queries needed (parsed at startup)
- Easy to add/remove users without restart
- Clear audit trail (env var history)

**Alternative Considered**: Database-backed users table
- **Rejected**: Over-engineered for 1-5 users; env var is sufficient

---

## 16. No Error Handling Fallback for Crypto

**Decision**: Fail fast if encryption key is missing or invalid

**Rationale**:
- Crypto errors are unrecoverable (data integrity risk)
- Silent defaults would hide misconfiguration
- Loud errors force operator to fix before running
- Single-user bot; no need for graceful degradation

---

## 17. Liquibase Master Changelog in XML, Changesets in SQL

**Decision**: XML master, SQL changesets (--liquibase formatted sql)

**Rationale**:
- XML is Liquibase standard for includes and versioning
- SQL changesets are easier to read and audit
- Hybrid approach gets best of both
- Changesets can be manually applied if needed

---

## 18. No CVS/Version Control for .env

**Decision**: .env gitignored, .env.example tracked

**Rationale**:
- Secrets (bot token, encryption key) never in git
- .env.example serves as documentation
- Operator creates .env locally from .example
- Standard practice for all 12-factor apps

---

## 19. @PostConstruct for Encryption Key Validation

**Decision**: Validate key in SecurityConfig with @PostConstruct

**Rationale**:
- Validation runs early in Spring lifecycle
- Fail at startup, not on first card operation
- Testable (can mock the validation)
- Clear that crypto is mandatory

---

## 20. No Transaction Rollback on Luhn Warning

**Decision**: Show warning dialog but allow user to opt-in to save, no automatic rollback

**Rationale**:
- User controls their data (transparency)
- Uzcard/Humo deviation from Luhn is known
- Warn, then trust user judgment
- Simpler than multi-step rollback logic

---

## Summary of Key Principles

1. **Security First**: Encrypt card numbers, validate key, log nothing sensitive
2. **Simplicity**: No unnecessary abstractions; one-user bot, not framework
3. **Explicitness**: Fail loudly, choose plaintext logging, centralize config
4. **Testability**: Constructor injection, mockable services, unit-testable crypto
5. **Auditability**: Liquibase migrations track all changes, no auto-DDL

