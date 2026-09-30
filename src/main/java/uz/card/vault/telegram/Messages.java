package uz.card.vault.telegram;

import org.springframework.stereotype.Component;

@Component
public class Messages {

	public String startMessage() {
		return """
			👋 Xush kelibsiz Vault botiga!

			Ushbu bot sizning plastik kartalaringizni xavfsiz saqlaydi va
			istalgan chatda ularga qulay kirish imkonini beradi.

			/help uchun buyruqlar ro'yxatini ko'ring.""";
	}

	public String helpMessage() {
		return """
			📋 Mavjud buyruqlar:

			/start - Tanishish
			/help - Bu xabar
			/add - Yangi karta qo'shish
			/cards - Barcha kartalarni ko'rish
			/delete - Karta o'chirish
			/cancel - Joriy jarayonni bekor qilish

			💡 Inlayn rejimi:
			Istalgan chatda @vault_bot yozing va kartani tanlang!""";
	}

	public String addCardStart() {
		return "📝 Karta raqamini kiriting:";
	}

	public String addCardExpiry() {
		return "📅 Kartaning muddati (MM/YY formatida):";
	}

	public String addCardHolder() {
		return "👤 Kartani egallaganining ismi (ixtiyoriy):";
	}

	public String addCardLabel() {
		return "🏷️ Karta belgisi/izohati (ixtiyoriy, masalan 'Asosiy', 'Ish'):";
	}

	public String selectCategory() {
		return "📂 Karta turi tanlang yoki avtomatik aniqlashni qabul qiling:";
	}

	public String cardAdded(String label, String last4) {
		return String.format("✅ Karta muvaffaqiyatli qo'shildi!\n📌 %s (%s)", label != null ? label : "Karta", last4);
	}

	public String cardAlreadyExists() {
		return "⚠️ Bu karta allaqachon saqlangan.";
	}

	public String noCards() {
		return "📭 Hali hech qanday karta qo'shilmagan. /add buyrug'i bilan karta qo'shishni boshlang.";
	}

	public String cardsList(int count) {
		return String.format("💳 Sizda %d karta bor:", count);
	}

	public String cardItem(String category, String last4, String label) {
		return String.format("• %s: ...%s %s", category, last4, label != null ? "(" + label + ")" : "");
	}

	public String showFullNumber() {
		return "🔓 To'liq raqamni ko'rsatish";
	}

	public String copyNumber() {
		return "📋 Raqamni ko'chirish";
	}

	public String deleteCard() {
		return "🗑️ O'chirish";
	}

	public String editLabel() {
		return "✏️ Belgini o'zgartirish";
	}

	public String cardNotFound() {
		return "❌ Karta topilmadi.";
	}

	public String cardDeleted() {
		return "✅ Karta o'chirildi.";
	}

	public String operationCancelled() {
		return "❌ Jarayon bekor qilindi.";
	}

	public String invalidInput() {
		return "❌ Noto'g'ri kiritish. Qayta urinib ko'ring.";
	}

	public String invalidCardNumber() {
		return "❌ Karta raqami noto'g'ri yoki Luhn tekshiruvisi muvaffaq bo'lmadi.";
	}

	public String invalidExpiry() {
		return "❌ Noto'g'ri muddati formati. MM/YY formatida kiriting.";
	}

	public String accessDenied() {
		return "🚫 Siz bu botdan foydalanishga vakolatli emassiz.";
	}

	public String inlineQueryNoResults() {
		return "Karta topilmadi";
	}

	public String inlineCardFormat(String category, String number, String expiry, String holder) {
		return String.format("""
			💳 %s
			```
			%s
			```
			📅 %s
			👤 %s""", category, number, expiry, holder != null && !holder.isEmpty() ? holder : "Nomi yo'q");
	}

	public String errorProcessing() {
		return "❌ Jarayonni bajarishda xato yuz berdi. Qayta urinib ko'ring.";
	}

	public String cardEditLabel() {
		return "✏️ Yangi belgini kiriting:";
	}

	public String labelUpdated() {
		return "✅ Belgisi yangilandi.";
	}

	public String luhnWarningUzcard() {
		return "⚠️ Uzcard raqamlarining ba'zilarida Luhn tekshiruvi muvaffaq bo'lmasligi oddiy. Karta shunaqa saqlashga majbur?";
	}

	public String luhnWarningHumo() {
		return "⚠️ Humo raqamlarining ba'zilarida Luhn tekshiruvi muvaffaq bo'lmasligi oddiy. Karta shunaqa saqlashga majbur?";
	}

	public String confirmSaveAnyway() {
		return "✅ Shunaqa saqlash\n❌ Bekor qilish";
	}
}
