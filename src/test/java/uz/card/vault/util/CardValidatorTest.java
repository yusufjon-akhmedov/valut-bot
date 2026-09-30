package uz.card.vault.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardValidatorTest {

	@Test
	void testValidLuhn() {
		// Valid test card numbers
		assertTrue(CardValidator.isValidLuhn("4111111111111111"));
		assertTrue(CardValidator.isValidLuhn("5555555555554444"));
		assertTrue(CardValidator.isValidLuhn("378282246310005"));
	}

	@Test
	void testInvalidLuhn() {
		assertFalse(CardValidator.isValidLuhn("1234567890123456"));
		assertFalse(CardValidator.isValidLuhn("4111111111111112"));
		assertFalse(CardValidator.isValidLuhn(""));
		assertFalse(CardValidator.isValidLuhn(null));
		assertFalse(CardValidator.isValidLuhn("abc1234567890123"));
	}

	@Test
	void testDetectVisa() {
		assertEquals(CardValidator.CardType.VISA, CardValidator.detectCardType("4111111111111111"));
		assertEquals(CardValidator.CardType.VISA, CardValidator.detectCardType("4532015112830366"));
	}

	@Test
	void testDetectMastercard() {
		assertEquals(CardValidator.CardType.MASTERCARD, CardValidator.detectCardType("5555555555554444"));
		assertEquals(CardValidator.CardType.MASTERCARD, CardValidator.detectCardType("5105105105105100"));
		assertEquals(CardValidator.CardType.MASTERCARD, CardValidator.detectCardType("2221001234567890"));
	}

	@Test
	void testDetectUzcard() {
		assertEquals(CardValidator.CardType.UZCARD, CardValidator.detectCardType("8600123456789012"));
	}

	@Test
	void testDetectHumo() {
		assertEquals(CardValidator.CardType.HUMO, CardValidator.detectCardType("9860123456789012"));
	}

	@Test
	void testMaskCardNumber() {
		assertEquals("**** **** **** 1111", CardValidator.maskCardNumber("4111111111111111"));
		assertEquals("**** **** **** 4444", CardValidator.maskCardNumber("5555555555554444"));
		assertEquals("****", CardValidator.maskCardNumber("123"));
	}

	@Test
	void testGetLast4() {
		assertEquals("1111", CardValidator.getLast4("4111111111111111"));
		assertEquals("4444", CardValidator.getLast4("5555555555554444"));
		assertEquals("", CardValidator.getLast4("123"));
	}
}
