package uz.card.vault.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

	private final AppProperties appProperties;

	@PostConstruct
	public void init() {
		validateCrypto();
	}

	private void validateCrypto() {
		if (!appProperties.getTelegram().isEnabled()) {
			return;
		}
		String encryptionKey = appProperties.getCrypto().getEncryptionKey();
		if (encryptionKey == null || encryptionKey.isBlank()) {
			throw new IllegalStateException(
				"CARD_ENCRYPTION_KEY must be set via environment variable. "
					+ "Generate with: openssl rand 32 | base64");
		}
		try {
			byte[] decodedKey = java.util.Base64.getDecoder().decode(encryptionKey);
			if (decodedKey.length != 32) {
				throw new IllegalStateException(
					"CARD_ENCRYPTION_KEY must be a base64-encoded 32-byte key (256-bit), "
						+ "got " + decodedKey.length + " bytes");
			}
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
				"CARD_ENCRYPTION_KEY is not valid base64: " + e.getMessage(), e);
		}
	}
}
