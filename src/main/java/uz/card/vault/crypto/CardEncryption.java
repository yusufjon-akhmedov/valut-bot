package uz.card.vault.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CardEncryption {

	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int TAG_LENGTH_BITS = 128;
	private static final int IV_LENGTH_BYTES = 12;

	private final CardEncryptionKeyProvider keyProvider;

	public EncryptedData encrypt(String plaintext) {
		try {
			byte[] iv = new byte[IV_LENGTH_BYTES];
			new SecureRandom().nextBytes(iv);

			SecretKey key = getSecretKey();
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, spec);

			byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
			return new EncryptedData(iv, ciphertext);
		} catch (Exception e) {
			throw new CryptoException("Failed to encrypt card number", e);
		}
	}

	public String decrypt(byte[] iv, byte[] ciphertext) {
		try {
			SecretKey key = getSecretKey();
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
			cipher.init(Cipher.DECRYPT_MODE, key, spec);

			byte[] plaintext = cipher.doFinal(ciphertext);
			return new String(plaintext);
		} catch (Exception e) {
			throw new CryptoException("Failed to decrypt card number (possible tampering)", e);
		}
	}

	private SecretKey getSecretKey() {
		byte[] decodedKey = keyProvider.getEncryptionKey();
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}

	public static class EncryptedData {
		public final byte[] iv;
		public final byte[] ciphertext;

		public EncryptedData(byte[] iv, byte[] ciphertext) {
			this.iv = iv;
			this.ciphertext = ciphertext;
		}
	}
}
