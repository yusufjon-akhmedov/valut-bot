package uz.card.vault.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.card.vault.crypto.CardEncryption;
import uz.card.vault.domain.Card;
import uz.card.vault.domain.CardCategory;
import uz.card.vault.repository.CardCategoryRepository;
import uz.card.vault.repository.CardRepository;
import uz.card.vault.util.CardValidator;

import java.security.MessageDigest;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CardService {

	private final CardRepository cardRepository;
	private final CardCategoryRepository categoryRepository;
	private final CardEncryption cardEncryption;

	public Card saveCard(
		Long ownerTelegramId,
		String cardNumber,
		int expiryMonth,
		int expiryYear,
		String holderName,
		String label,
		Integer overrideCategoryId) {

		String cardNumberHash = hashCardNumber(cardNumber);

		// Check for duplicate
		Optional<Card> existing = cardRepository.findByOwnerAndHash(ownerTelegramId, cardNumberHash);
		if (existing.isPresent()) {
			throw new IllegalArgumentException("Card already saved");
		}

		// Detect or use override category
		Integer categoryId = overrideCategoryId;
		if (categoryId == null) {
			CardValidator.CardType detectedType = CardValidator.detectCardType(cardNumber);
			CategoryResult category = findCategoryByCode(detectedType.code);
			categoryId = category.id;
		}

		CardCategory cardCategory = categoryRepository.findById(categoryId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid category"));

		// Encrypt and save
		CardEncryption.EncryptedData encrypted = cardEncryption.encrypt(cardNumber);

		Card card = new Card();
		card.setOwnerTelegramId(ownerTelegramId);
		card.setCategory(cardCategory);
		card.setCardNumberEncrypted(combineIvAndCiphertext(encrypted.iv, encrypted.ciphertext));
		card.setCardNumberHash(cardNumberHash);
		card.setLast4(CardValidator.getLast4(cardNumber));
		card.setExpiryMonth(expiryMonth);
		card.setExpiryYear(expiryYear);
		card.setHolderName(holderName);
		card.setLabel(label);

		return cardRepository.save(card);
	}

	@Transactional(readOnly = true)
	public List<Card> getCardsByUser(Long userTelegramId) {
		return cardRepository.findByOwnerTelegramIdOrderByCreatedAtDesc(userTelegramId);
	}

	@Transactional(readOnly = true)
	public List<Card> getCardsByUserAndCategory(Long userTelegramId, Integer categoryId) {
		return cardRepository.findByOwnerTelegramIdAndCategoryCategoryIdOrderByCreatedAtDesc(
			userTelegramId, categoryId);
	}

	@Transactional(readOnly = true)
	public Optional<Card> getCardById(UUID id, Long userTelegramId) {
		Optional<Card> card = cardRepository.findById(id);
		if (card.isPresent() && card.get().getOwnerTelegramId().equals(userTelegramId)) {
			return card;
		}
		return Optional.empty();
	}

	public void deleteCard(UUID id, Long userTelegramId) {
		Optional<Card> card = cardRepository.findById(id);
		if (card.isPresent() && card.get().getOwnerTelegramId().equals(userTelegramId)) {
			cardRepository.deleteById(id);
		} else {
			throw new IllegalArgumentException("Card not found");
		}
	}

	public String decryptCardNumber(Card card, Long userTelegramId) {
		if (!card.getOwnerTelegramId().equals(userTelegramId)) {
			throw new IllegalArgumentException("Unauthorized access to card");
		}

		byte[] ivAndCiphertext = card.getCardNumberEncrypted();
		byte[] iv = new byte[12];
		System.arraycopy(ivAndCiphertext, 0, iv, 0, 12);

		byte[] ciphertext = new byte[ivAndCiphertext.length - 12];
		System.arraycopy(ivAndCiphertext, 12, ciphertext, 0, ciphertext.length);

		return cardEncryption.decrypt(iv, ciphertext);
	}

	@Transactional(readOnly = true)
	public CategoryResult findCategoryByCode(String code) {
		CardCategory category = categoryRepository.findByCode(code)
			.orElseThrow(() -> new IllegalArgumentException("Category not found: " + code));
		return new CategoryResult(category.getId(), category.getCode(), category.getName());
	}

	@Transactional(readOnly = true)
	public List<CategoryResult> getAllCategories() {
		return categoryRepository.findAll().stream()
			.map(c -> new CategoryResult(c.getId(), c.getCode(), c.getName()))
			.toList();
	}

	private String hashCardNumber(String cardNumber) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(cardNumber.getBytes());
			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException("Failed to hash card number", e);
		}
	}

	private byte[] combineIvAndCiphertext(byte[] iv, byte[] ciphertext) {
		byte[] combined = new byte[iv.length + ciphertext.length];
		System.arraycopy(iv, 0, combined, 0, iv.length);
		System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
		return combined;
	}

	public record CategoryResult(Integer id, String code, String name) {
	}
}
