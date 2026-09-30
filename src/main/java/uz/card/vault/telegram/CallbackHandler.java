package uz.card.vault.telegram;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.card.vault.domain.Card;
import uz.card.vault.service.CardService;
import uz.card.vault.service.ConversationStateService;
import uz.card.vault.util.CardValidator;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CallbackHandler {

	private final CardService cardService;
	private final ConversationStateService stateService;
	private final Messages messages;
	private final KeyboardBuilder keyboards;
	private final ObjectMapper objectMapper;

	public void handle(CallbackQuery query, Long userId, BotApi botApi) throws Exception {
		String data = query.getData();
		Long chatId = query.getMessage().getChatId();
		Integer messageId = query.getMessage().getMessageId();

		try {
			if (data.startsWith("cat:")) {
				handleCategorySelection(data, chatId, userId, messageId, botApi);
			} else if (data.startsWith("show:")) {
				handleShowFullNumber(data, chatId, userId, botApi);
			} else if (data.startsWith("edit:")) {
				handleEditLabel(data, chatId, userId, messageId, botApi);
			} else if (data.startsWith("del:")) {
				handleDelete(data, chatId, userId, messageId, botApi);
			} else if (data.startsWith("luhn:")) {
				handleLuhnConfirm(data, chatId, userId, messageId, botApi);
			} else if (data.equals("cancel")) {
				handleCancel(chatId, userId, botApi);
			}

			// Answer callback
			AnswerCallbackQuery answer = new AnswerCallbackQuery(query.getId());
			botApi.execute(answer);
		} catch (Exception e) {
			log.error("Error in callback handler: {}", e.getMessage(), e);
			AnswerCallbackQuery answer = new AnswerCallbackQuery(query.getId());
			answer.setText("❌ Xato yuz berdi");
			botApi.execute(answer);
		}
	}

	private void handleCategorySelection(String data, Long chatId, Long userId, Integer messageId, BotApi botApi) throws Exception {
		String categoryCode = data.substring(4);

		Optional<CommandHandler.DialogState> state = stateService.getStateData(userId, CommandHandler.DialogState.class);
		if (state.isEmpty()) {
			return;
		}

		CommandHandler.DialogState dialogState = state.get();

		Integer categoryId = null;
		if (!categoryCode.equals("AUTO")) {
			CardService.CategoryResult category = cardService.findCategoryByCode(categoryCode);
			categoryId = category.id();
		}

		try {
			Card savedCard = cardService.saveCard(
				userId,
				dialogState.cardNumber,
				dialogState.expiryMonth,
				dialogState.expiryYear,
				dialogState.holderName,
				dialogState.label,
				categoryId);

			stateService.clearState(userId);

			// Delete the keyboard message
			try {
				DeleteMessage deleteMsg = new DeleteMessage(String.valueOf(chatId), messageId);
				botApi.execute(deleteMsg);
			} catch (Exception e) {
				log.debug("Could not delete message", e);
			}

			String successMsg = messages.cardAdded(savedCard.getLabel(), savedCard.getLast4());
			SendMessage msg = new SendMessage(String.valueOf(chatId), successMsg);
			botApi.execute(msg);
		} catch (IllegalArgumentException e) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardAlreadyExists());
			botApi.execute(msg);
		}
	}

	private void handleShowFullNumber(String data, Long chatId, Long userId, BotApi botApi) throws Exception {
		String cardIdStr = data.substring(5);
		UUID cardId = UUID.fromString(cardIdStr);

		Optional<Card> card = cardService.getCardById(cardId, userId);
		if (card.isEmpty()) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardNotFound());
			botApi.execute(msg);
			return;
		}

		String fullNumber = cardService.decryptCardNumber(card.get(), userId);
		String expiry = String.format("%02d/%02d", card.get().getExpiryMonth(), card.get().getExpiryYear() % 100);
		String holderName = card.get().getHolderName() != null ? card.get().getHolderName() : "N/A";

		String text = messages.inlineCardFormat(
			card.get().getCategory().getName(),
			fullNumber,
			expiry,
			holderName);

		SendMessage msg = new SendMessage(String.valueOf(chatId), text);
		msg.setParseMode("Markdown");
		botApi.execute(msg);
	}

	private void handleEditLabel(String data, Long chatId, Long userId, Integer messageId, BotApi botApi) throws Exception {
		String cardIdStr = data.substring(5);
		UUID cardId = UUID.fromString(cardIdStr);

		Optional<Card> card = cardService.getCardById(cardId, userId);
		if (card.isEmpty()) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardNotFound());
			botApi.execute(msg);
			return;
		}

		CommandHandler.DialogState state = new CommandHandler.DialogState("edit_label", cardIdStr);
		stateService.setState(userId, "edit_label", state);

		try {
			DeleteMessage deleteMsg = new DeleteMessage(String.valueOf(chatId), messageId);
			botApi.execute(deleteMsg);
		} catch (Exception e) {
			log.debug("Could not delete message", e);
		}

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardEditLabel());
		botApi.execute(msg);
	}

	private void handleDelete(String data, Long chatId, Long userId, Integer messageId, BotApi botApi) throws Exception {
		String cardIdStr = data.substring(4);
		UUID cardId = UUID.fromString(cardIdStr);

		Optional<Card> card = cardService.getCardById(cardId, userId);
		if (card.isEmpty()) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardNotFound());
			botApi.execute(msg);
			return;
		}

		cardService.deleteCard(cardId, userId);

		try {
			DeleteMessage deleteMsg = new DeleteMessage(String.valueOf(chatId), messageId);
			botApi.execute(deleteMsg);
		} catch (Exception e) {
			log.debug("Could not delete message", e);
		}

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardDeleted());
		botApi.execute(msg);
	}

	private void handleLuhnConfirm(String data, Long chatId, Long userId, Integer messageId, BotApi botApi) throws Exception {
		String action = data.substring(5);

		Optional<CommandHandler.DialogState> state = stateService.getStateData(userId, CommandHandler.DialogState.class);
		if (state.isEmpty()) {
			return;
		}

		CommandHandler.DialogState dialogState = state.get();

		if (action.equals("yes")) {
			// Continue with category selection
			DialogState newState = new DialogState(
				"category_confirm",
				dialogState.cardNumber,
				null,
				null,
				null,
				null);
			stateService.setState(userId, "category_confirm", newState);

			try {
				DeleteMessage deleteMsg = new DeleteMessage(String.valueOf(chatId), messageId);
				botApi.execute(deleteMsg);
			} catch (Exception e) {
				log.debug("Could not delete message", e);
			}

			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.selectCategory());
			msg.setReplyMarkup(keyboards.categoryButtons(cardService.getAllCategories()));
			botApi.execute(msg);
		} else {
			// Cancel
			stateService.clearState(userId);

			try {
				DeleteMessage deleteMsg = new DeleteMessage(String.valueOf(chatId), messageId);
				botApi.execute(deleteMsg);
			} catch (Exception e) {
				log.debug("Could not delete message", e);
			}

			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.operationCancelled());
			botApi.execute(msg);
		}
	}

	private void handleCancel(Long chatId, Long userId, BotApi botApi) throws Exception {
		stateService.clearState(userId);

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.operationCancelled());
		botApi.execute(msg);
	}

	public static class DialogState {
		public String step;
		public String cardNumber;
		public Integer expiryMonth;
		public Integer expiryYear;
		public String holderName;
		public String label;

		public DialogState() {}

		public DialogState(String step, String cardNumber, Integer expiryMonth, Integer expiryYear, String holderName, String label) {
			this.step = step;
			this.cardNumber = cardNumber;
			this.expiryMonth = expiryMonth;
			this.expiryYear = expiryYear;
			this.holderName = holderName;
			this.label = label;
		}
	}
}
