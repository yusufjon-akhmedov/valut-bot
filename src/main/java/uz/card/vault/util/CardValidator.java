package uz.card.vault.util;

public class CardValidator {

	public static boolean isValidLuhn(String cardNumber) {
		if (cardNumber == null || !cardNumber.matches("\\d+")) {
			return false;
		}

		int sum = 0;
		boolean isEven = false;

		for (int i = cardNumber.length() - 1; i >= 0; i--) {
			int digit = Character.getNumericValue(cardNumber.charAt(i));

			if (isEven) {
				digit *= 2;
				if (digit > 9) {
					digit -= 9;
				}
			}

			sum += digit;
			isEven = !isEven;
		}

		return sum % 10 == 0;
	}

	public enum CardType {
		VISA("VISA", "Visa"),
		MASTERCARD("MASTERCARD", "Mastercard"),
		UZCARD("UZCARD", "Uzcard"),
		HUMO("HUMO", "Humo"),
		UNKNOWN("UNKNOWN", "Unknown");

		public final String code;
		public final String displayName;

		CardType(String code, String displayName) {
			this.code = code;
			this.displayName = displayName;
		}
	}

	public static CardType detectCardType(String cardNumber) {
		if (cardNumber == null || cardNumber.length() < 4) {
			return CardType.UNKNOWN;
		}

		String first2 = cardNumber.substring(0, 2);
		String first4 = cardNumber.substring(0, 4);
		String first6 = cardNumber.substring(0, Math.min(6, cardNumber.length()));

		// Visa: starts with 4
		if (cardNumber.startsWith("4")) {
			return CardType.VISA;
		}

		// Mastercard: 51-55 or 2221-2720
		if (first2.matches("5[1-5]")) {
			return CardType.MASTERCARD;
		}
		try {
			int first4Int = Integer.parseInt(first4);
			if (first4Int >= 2221 && first4Int <= 2720) {
				return CardType.MASTERCARD;
			}
		} catch (NumberFormatException e) {
			// continue
		}

		// Uzcard: starts with 8600
		if (cardNumber.startsWith("8600")) {
			return CardType.UZCARD;
		}

		// Humo: starts with 9860
		if (cardNumber.startsWith("9860")) {
			return CardType.HUMO;
		}

		return CardType.UNKNOWN;
	}

	public static String maskCardNumber(String cardNumber) {
		if (cardNumber == null || cardNumber.length() < 4) {
			return "****";
		}
		String last4 = cardNumber.substring(cardNumber.length() - 4);
		return "**** **** **** " + last4;
	}

	public static String getLast4(String cardNumber) {
		if (cardNumber == null || cardNumber.length() < 4) {
			return "";
		}
		return cardNumber.substring(cardNumber.length() - 4);
	}
}
