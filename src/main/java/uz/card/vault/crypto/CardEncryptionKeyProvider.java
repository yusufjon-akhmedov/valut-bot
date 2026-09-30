package uz.card.vault.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.card.vault.config.AppProperties;

@Component
@RequiredArgsConstructor
public class CardEncryptionKeyProvider {

	private final AppProperties appProperties;

	public byte[] getEncryptionKey() {
		return appProperties.getCrypto().getEncryptionKeyBytes();
	}
}
