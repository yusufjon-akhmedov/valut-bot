package uz.card.vault.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CardEncryptionTest {

	@Mock
	private CardEncryptionKeyProvider keyProvider;

	private CardEncryption encryption;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Create a 32-byte key
		byte[] key = new byte[32];
		for (int i = 0; i < 32; i++) {
			key[i] = (byte) i;
		}
		when(keyProvider.getEncryptionKey()).thenReturn(key);
		encryption = new CardEncryption(keyProvider);
	}

	@Test
	void testEncryptAndDecrypt() {
		String plaintext = "4532015112830366";

		CardEncryption.EncryptedData encrypted = encryption.encrypt(plaintext);
		assertNotNull(encrypted.iv);
		assertNotNull(encrypted.ciphertext);
		assertEquals(12, encrypted.iv.length);
		assertTrue(encrypted.ciphertext.length > 0);

		String decrypted = encryption.decrypt(encrypted.iv, encrypted.ciphertext);
		assertEquals(plaintext, decrypted);
	}

	@Test
	void testEncryptionDeterministic() {
		String plaintext = "4532015112830366";

		CardEncryption.EncryptedData encrypted1 = encryption.encrypt(plaintext);
		CardEncryption.EncryptedData encrypted2 = encryption.encrypt(plaintext);

		assertFalse(java.util.Arrays.equals(encrypted1.iv, encrypted2.iv));
		assertFalse(java.util.Arrays.equals(encrypted1.ciphertext, encrypted2.ciphertext));
	}

	@Test
	void testTamperDetection() {
		String plaintext = "4532015112830366";
		CardEncryption.EncryptedData encrypted = encryption.encrypt(plaintext);

		// Tamper with ciphertext
		byte[] tamperedCiphertext = encrypted.ciphertext.clone();
		if (tamperedCiphertext.length > 0) {
			tamperedCiphertext[0] ^= 0xFF;
		}

		assertThrows(CryptoException.class, () -> {
			encryption.decrypt(encrypted.iv, tamperedCiphertext);
		});
	}

	@Test
	void testWrongKey() {
		String plaintext = "4532015112830366";
		CardEncryption.EncryptedData encrypted = encryption.encrypt(plaintext);

		// Use wrong key
		byte[] wrongKey = new byte[32];
		for (int i = 0; i < 32; i++) {
			wrongKey[i] = (byte) (255 - i);
		}
		when(keyProvider.getEncryptionKey()).thenReturn(wrongKey);

		CardEncryption wrongEncryption = new CardEncryption(keyProvider);
		assertThrows(CryptoException.class, () -> {
			wrongEncryption.decrypt(encrypted.iv, encrypted.ciphertext);
		});
	}
}
