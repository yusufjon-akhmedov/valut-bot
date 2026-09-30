package uz.card.vault.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppPropertiesTest {

	@Test
	void testUserIdAllowlist() {
		AppProperties props = new AppProperties();
		AppProperties.Telegram telegram = props.getTelegram();
		telegram.setAllowedUserIds("123456,789012,345678");

		assertTrue(telegram.isUserAllowed(123456L));
		assertTrue(telegram.isUserAllowed(789012L));
		assertTrue(telegram.isUserAllowed(345678L));
		assertFalse(telegram.isUserAllowed(999999L));
	}

	@Test
	void testEmptyUserIdList() {
		AppProperties props = new AppProperties();
		AppProperties.Telegram telegram = props.getTelegram();
		telegram.setAllowedUserIds("");

		assertFalse(telegram.isUserAllowed(123456L));
	}

	@Test
	void testNullUserIdList() {
		AppProperties props = new AppProperties();
		AppProperties.Telegram telegram = props.getTelegram();
		telegram.setAllowedUserIds(null);

		assertFalse(telegram.isUserAllowed(123456L));
	}

	@Test
	void testInvalidUserIdThrows() {
		AppProperties props = new AppProperties();
		AppProperties.Telegram telegram = props.getTelegram();
		telegram.setAllowedUserIds("123456,invalid,789012");

		assertThrows(IllegalStateException.class, telegram::getAllowedUserIdsSet);
	}

	@Test
	void testEncryptionKeyValidation() {
		AppProperties props = new AppProperties();
		AppProperties.Crypto crypto = props.getCrypto();

		// Valid 32-byte key in base64
		byte[] key = new byte[32];
		String validKey = java.util.Base64.getEncoder().encodeToString(key);
		crypto.setEncryptionKey(validKey);

		byte[] decodedKey = crypto.getEncryptionKeyBytes();
		assertEquals(32, decodedKey.length);
	}

	@Test
	void testEncryptionKeyInvalidBase64Throws() {
		AppProperties props = new AppProperties();
		AppProperties.Crypto crypto = props.getCrypto();
		crypto.setEncryptionKey("not-valid-base64!@#$");

		assertThrows(IllegalArgumentException.class, crypto::getEncryptionKeyBytes);
	}
}
