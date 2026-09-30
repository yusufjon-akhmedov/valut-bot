package uz.card.vault.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.card.vault.service.CardService.CategoryResult;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeyboardBuilder {

	public InlineKeyboardMarkup categoryButtons(List<CategoryResult> categories) {
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();

		for (CategoryResult cat : categories) {
			List<InlineKeyboardButton> row = new ArrayList<>();
			InlineKeyboardButton btn = new InlineKeyboardButton();
			btn.setText(cat.name());
			btn.setCallbackData("cat:" + cat.code());
			row.add(btn);
			rows.add(row);
		}

		// Auto-detect option
		List<InlineKeyboardButton> autoRow = new ArrayList<>();
		InlineKeyboardButton autoBtn = new InlineKeyboardButton();
		autoBtn.setText("🤖 Avtomatik aniqlash");
		autoBtn.setCallbackData("cat:AUTO");
		autoRow.add(autoBtn);
		rows.add(autoRow);

		markup.setKeyboard(rows);
		return markup;
	}

	public InlineKeyboardMarkup cardActionButtons(String cardId) {
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();

		// Row 1: Show full number
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		InlineKeyboardButton showBtn = new InlineKeyboardButton();
		showBtn.setText("🔓 To'liq raqam");
		showBtn.setCallbackData("show:" + cardId);
		row1.add(showBtn);
		rows.add(row1);

		// Row 2: Edit label
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		InlineKeyboardButton editBtn = new InlineKeyboardButton();
		editBtn.setText("✏️ Belgini o'zgartirish");
		editBtn.setCallbackData("edit:" + cardId);
		row2.add(editBtn);
		rows.add(row2);

		// Row 3: Delete
		List<InlineKeyboardButton> row3 = new ArrayList<>();
		InlineKeyboardButton delBtn = new InlineKeyboardButton();
		delBtn.setText("🗑️ O'chirish");
		delBtn.setCallbackData("del:" + cardId);
		row3.add(delBtn);
		rows.add(row3);

		markup.setKeyboard(rows);
		return markup;
	}

	public InlineKeyboardMarkup confirmButtons(String prefix) {
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();

		List<InlineKeyboardButton> row = new ArrayList<>();

		InlineKeyboardButton yesBtn = new InlineKeyboardButton();
		yesBtn.setText("✅ Ha");
		yesBtn.setCallbackData(prefix + ":yes");
		row.add(yesBtn);

		InlineKeyboardButton noBtn = new InlineKeyboardButton();
		noBtn.setText("❌ Yo'q");
		noBtn.setCallbackData(prefix + ":no");
		row.add(noBtn);

		rows.add(row);
		markup.setKeyboard(rows);
		return markup;
	}

	public InlineKeyboardMarkup cancelButton() {
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();

		List<InlineKeyboardButton> row = new ArrayList<>();
		InlineKeyboardButton cancelBtn = new InlineKeyboardButton();
		cancelBtn.setText("❌ Bekor qilish");
		cancelBtn.setCallbackData("cancel");
		row.add(cancelBtn);

		rows.add(row);
		markup.setKeyboard(rows);
		return markup;
	}
}
