package uz.card.vault.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Telegram telegram = new Telegram();
	private Crypto crypto = new Crypto();

	@Getter
	@Setter
	public static class Telegram {
		private boolean enabled = false;
		private String botToken = "";
		private String allowedUserIds = "";

		public Set<Long> getAllowedUserIdsSet() {
			Set<Long> ids = new HashSet<>();
			if (allowedUserIds == null || allowedUserIds.isBlank()) {
				return ids;
			}
			String[] parts = allowedUserIds.split(",");
			for (String part : parts) {
				try {
					ids.add(Long.parseLong(part.trim()));
				} catch (NumberFormatException e) {
					throw new IllegalStateException("Invalid user ID in ALLOWED_USER_IDS: " + part, e);
				}
			}
			return ids;
		}

		public boolean isUserAllowed(long userId) {
			return getAllowedUserIdsSet().contains(userId);
		}
	}

	@Getter
	@Setter
	public static class Crypto {
		private String encryptionKey = "";

		public byte[] getEncryptionKeyBytes() {
			if (encryptionKey == null || encryptionKey.isBlank()) {
				throw new IllegalStateException("Encryption key not configured");
			}
			return java.util.Base64.getDecoder().decode(encryptionKey);
		}
	}
}
