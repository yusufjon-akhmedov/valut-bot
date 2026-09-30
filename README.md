# 💳 Vault Bot — Telegram Plastic Card Manager

Vault is a secure Telegram bot that allows you to store and manage your plastic cards safely. Share cards into any chat using Telegram's inline mode, with AES-256-GCM encryption protecting the data at rest.

**⚠️ Financial Data**: This bot handles card numbers and must be treated with production-grade security.

## Features

- **Secure Storage**: Cards encrypted at rest with AES-256-GCM, stored in PostgreSQL
- **Category Detection**: Automatically detects VISA, Mastercard, Uzcard, Humo from card number
- **Inline Mode**: Type `@bot_username` in any chat and instantly share a chosen card
- **Luhn Validation**: Validates card numbers; allows Uzcard/Humo even if Luhn fails (known issue with local ranges)
- **Personal Results**: Inline mode is personal (is_personal=true, cache_time=0) — only you see your cards
- **Access Control**: Configure allowed Telegram user IDs in environment; everyone else is denied
- **Multi-language Ready**: All UI text in one Messages class (currently Uzbek Latin)

## Architecture

```
uz.card.vault
├── config/          — AppProperties, SecurityConfig
├── crypto/          — AES-256-GCM encryption, key validation
├── domain/          — JPA entities (User, Card, CardCategory, ConversationState)
├── repository/      — Spring Data JPA repositories
├── service/         — CardService, ConversationStateService
├── telegram/        — Bot layer (VaultBot, handlers, keyboards, messages)
└── util/            — CardValidator (Luhn, masking, detection)
```

**Database**: PostgreSQL (Liquibase migrations in `src/main/resources/db/changelog/`)
**Testing**: Unit tests with JUnit 5; integration uses H2 in-memory DB

## Setup & Running

### 1. Environment Variables

Copy `.env.example` to `.env` and fill in:

```bash
DB_USERNAME=vault_user
DB_PASSWORD=vault_password
TELEGRAM_ENABLED=true
TELEGRAM_BOT_TOKEN=<your bot token from @BotFather>
ALLOWED_USER_IDS=<your telegram user id, comma-separated>
CARD_ENCRYPTION_KEY=<base64-encoded 32-byte AES key>
```

**Generate encryption key:**
```bash
openssl rand 32 | base64
```

### 2. Register Bot with @BotFather

1. Chat with @BotFather on Telegram
2. `/newbot` → name your bot → get token
3. `/setinline` → select your bot → enter "Choose a card to share"
4. (Optional) `/setdefault_administrator_rights` to restrict admin rights

### 3. Docker Compose (Local Development)

```bash
# Start PostgreSQL
docker compose up -d

# Run the app
./gradlew bootRun
```

The app will:
- Apply Liquibase migrations automatically
- Seed card categories (VISA, MASTERCARD, UZCARD, HUMO)
- Be available at `http://localhost:8080`
- Health check at `/actuator/health`

### 4. Build & Test

```bash
# Run all tests
./gradlew test

# Build JAR
./gradlew build

# Run JAR
java -jar build/libs/vault-*.jar
```

## Bot Commands

| Command | Description |
|---------|-------------|
| `/start` | Welcome message |
| `/help` | List of commands |
| `/add` | Guided dialog to add a new card |
| `/cards` | List your cards (masked by default) |
| `/delete` | Delete a card |
| `/cancel` | Cancel current operation |
| Inline: `@bot_name` | Share a card into any chat |

### /add Dialog Flow
1. Card number (13–19 digits, Luhn validated)
2. Expiry (MM/YY format)
3. Holder name (optional)
4. Label (optional, e.g., "Personal", "Work")
5. Category (auto-detected or manually select via inline buttons)

Each step can be cancelled with /cancel.

## Security & Data Handling

### Encryption
- **Algorithm**: AES-256-GCM with random 12-byte IV per card
- **Key Storage**: Environment variable `CARD_ENCRYPTION_KEY`, loaded at startup
- **Key Rotation**: Not yet implemented (manual)
- **IV + Ciphertext**: Stored together in `card_number_encrypted` BYTEA column (IV first 12 bytes)

### Database Schema
```sql
cards (
  card_number_encrypted BYTEA NOT NULL,  -- encrypted card number
  card_number_hash VARCHAR(64) NOT NULL, -- SHA-256 for duplicate detection
  last4 VARCHAR(4) NOT NULL,             -- last 4 digits (plaintext, for UI)
  ...
)
```

### What's NOT Stored
- CVV / CVC (never requested)
- PIN codes (never requested)
- Full card numbers (only encrypted)

### Validation & Startup
- App fails loudly if `CARD_ENCRYPTION_KEY` is missing or invalid
- Only the configured `ALLOWED_USER_IDS` can use the bot; others receive a refusal message
- All exceptions are caught; card numbers are never logged

### Logging
- Card numbers are never logged (masked, encrypted, or abstracted)
- Sensitive values (token, key) are never logged
- Application logs go to console and can be redirected to files

## Deployment

### Docker Multi-Stage Build

```dockerfile
# Dockerfile included; build with:
docker build -t vault:latest .

# Run with:
docker run -e TELEGRAM_BOT_TOKEN=... \
           -e CARD_ENCRYPTION_KEY=... \
           -e ALLOWED_USER_IDS=... \
           -e DB_USERNAME=... \
           -e DB_PASSWORD=... \
           vault:latest
```

### Environment Profiles
- **dev** (default): Logs to console; DockerCompose auto-starts Postgres
- **prod**: Configure via environment; connect to external Postgres

## Known Limitations & TODOs

1. **Key Rotation**: Implement encrypted key versioning for key rotation without full re-encryption
2. **Rate Limiting**: No per-user rate limits on /add or inline queries
3. **Inline Result Pagination**: Telegram limits to 50 results; cards beyond that are not shown
4. **Edit Label**: Label editing in mid-dialog is not fully implemented
5. **Uzbek Only**: UI is hardcoded in Uzbek Latin; internationalization is future work

## Dependencies

- **Spring Boot**: 4.0.8
- **Java**: 25
- **Database**: PostgreSQL 16+ (or H2 for tests)
- **Telegram**: telegrambots 5.7.1 (long-polling)
- **Encryption**: Built-in `javax.crypto`
- **Liquibase**: 4.29.1
- **Testing**: JUnit 5, Testcontainers, Mockito

## Troubleshooting

### App won't start: "CARD_ENCRYPTION_KEY must be set"
- Generate key: `openssl rand 32 | base64`
- Set `CARD_ENCRYPTION_KEY` environment variable
- Restart

### Postgres connection fails
- Check `DB_USERNAME`, `DB_PASSWORD`, `DB_HOSTNAME` (default: localhost:5432)
- If using docker compose: `docker compose logs postgres`
- Ensure `docker compose up -d` ran successfully

### Bot doesn't respond
- Verify `TELEGRAM_BOT_TOKEN` is correct (from @BotFather)
- Verify `ALLOWED_USER_IDS` contains your Telegram user ID
- Check app logs: `./gradlew bootRun 2>&1 | grep -i error`

### Tests fail
- Run: `./gradlew clean build`
- Full output: `./gradlew test --info`
- Tests use H2 in-memory DB; no external setup needed

## Testing

```bash
# Run all unit tests
./gradlew test

# Run a specific test class
./gradlew test --tests CardValidatorTest

# View test report
open build/reports/tests/test/index.html
```

**Coverage**:
- Luhn validation (edge cases)
- Card type detection (Visa, Mastercard, Uzcard, Humo)
- Card masking
- AES-GCM encryption/decryption + tamper detection
- Access control (allowed user IDs)

## Development

### Adding a New Card Type
1. Add to `CardValidator.CardType` enum
2. Add detection logic in `CardValidator.detectCardType()`
3. Seed the category in `002-seed-categories.sql`
4. Update Messages.lua for UI text

### Adding a New Command
1. Add handler method to `CommandHandler`
2. Route in `CommandHandler.handle()`
3. Add messages in `Messages` class
4. Write unit tests

### Logging
- Use `@Slf4j` from Lombok
- Avoid logging card numbers; use `card.getLast4()` or `CardValidator.maskCardNumber()`

## License

Private. For personal use only.

---

**Built**: 2026-10-01  
**Version**: 0.0.1-SNAPSHOT
