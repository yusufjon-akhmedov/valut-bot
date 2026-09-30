package uz.card.vault.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.card.vault.domain.Card;
import uz.card.vault.domain.User;
import uz.card.vault.repository.UserRepository;
import uz.card.vault.service.CardService;
import uz.card.vault.service.ConversationStateService;
import uz.card.vault.util.CardValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandHandler {

	private final CardService cardService;
	private final ConversationStateService stateService;
	private final UserRepository userRepository;
	private final Messages messages;
	private final KeyboardBuilder keyboards;

	public void handle(String command, String args, Long chatId, Long userId, BotApi botApi) throws Exception {
		ensureUserExists(userId);

		switch (command) {
			case "/start" -> handleStart(chatId, botApi);
			case "/help" -> handleHelp(chatId, botApi);
			case "/add" -> handleAddStart(chatId, userId, botApi);
			case "/cards" -> handleCards(chatId, userId, botApi);
			case "/delete" -> handleDeleteStart(chatId, userId, botApi);
			case "/cancel" -> handleCancel(chatId, userId, botApi);
			default -> {
				SendMessage msg = new SendMessage(String.valueOf(chatId), "❓ Noma'lum buyruq. /help ko'ring.");
				botApi.execute(msg);
			}
		}
	}

	public void handleTextInput(String text, Long chatId, Long userId, BotApi botApi) throws Exception {
		Optional<DialogState> state = stateService.getStateData(userId, DialogState.class);

		if (state.isEmpty()) {
			return;
		}

		DialogState currentState = state.get();
		switch (currentState.step) {
			case "card_number" -> handleCardNumberInput(text, chatId, userId, currentState, botApi);
			case "expiry" -> handleExpiryInput(text, chatId, userId, currentState, botApi);
			case "holder_name" -> handleHolderNameInput(text, chatId, userId, currentState, botApi);
			case "label" -> handleLabelInput(text, chatId, userId, currentState, botApi);
			case "edit_label" -> handleEditLabelInput(text, chatId, userId, currentState, botApi);
		}
	}

	private void handleStart(Long chatId, BotApi botApi) throws Exception {
		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.startMessage());
		botApi.execute(msg);
	}

	private void handleHelp(Long chatId, BotApi botApi) throws Exception {
		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.helpMessage());
		botApi.execute(msg);
	}

	private void handleAddStart(Long chatId, Long userId, BotApi botApi) throws Exception {
		DialogState state = new DialogState("card_number", null);
		stateService.setState(userId, "card_number", state);

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.addCardStart());
		botApi.execute(msg);
	}

	private void handleCardNumberInput(String text, Long chatId, Long userId, DialogState state, BotApi botApi) throws Exception {
		String cardNumber = text.replaceAll("\\s+", "");

		// Validate length
		if (!cardNumber.matches("\\d{13,19}")) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.invalidCardNumber());
			botApi.execute(msg);
			return;
		}

		// Check Luhn for most cards, warn for Uzcard/Humo
		CardValidator.CardType cardType = CardValidator.detectCardType(cardNumber);
		boolean luhnValid = CardValidator.isValidLuhn(cardNumber);

		if (!luhnValid && (cardType == CardValidator.CardType.UZCARD || cardType == CardValidator.CardType.HUMO)) {
			DialogState newState = new DialogState("luhn_confirm", cardNumber);
			stateService.setState(userId, "luhn_confirm", newState);

			String warning = cardType == CardValidator.CardType.UZCARD
				? messages.luhnWarningUzcard()
				: messages.luhnWarningHumo();

			SendMessage msg = new SendMessage(String.valueOf(chatId), warning);
			msg.setReplyMarkup(keyboards.confirmButtons("luhn"));
			botApi.execute(msg);
			return;
		}

		if (!luhnValid) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.invalidCardNumber());
			botApi.execute(msg);
			return;
		}

		promptExpiry(chatId, userId, cardNumber, botApi);
	}

	private void promptExpiry(Long chatId, Long userId, String cardNumber, BotApi botApi) throws Exception {
		DialogState state = new DialogState("expiry", cardNumber);
		stateService.setState(userId, "expiry", state);

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.addCardExpiry());
		botApi.execute(msg);
	}

	private void handleExpiryInput(String text, Long chatId, Long userId, DialogState state, BotApi botApi) throws Exception {
		String[] parts = text.split("/");
		if (parts.length != 2) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.invalidExpiry());
			botApi.execute(msg);
			return;
		}

		try {
			int month = Integer.parseInt(parts[0].trim());
			int year = Integer.parseInt(parts[1].trim());

			if (month < 1 || month > 12 || year < 0 || year > 99) {
				SendMessage msg = new SendMessage(String.valueOf(chatId), messages.invalidExpiry());
				botApi.execute(msg);
				return;
			}

			DialogState newState = new DialogState("holder_name", state.cardNumber, month, year, null);
			stateService.setState(userId, "holder_name", newState);

			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.addCardHolder());
			botApi.execute(msg);
		} catch (NumberFormatException e) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.invalidExpiry());
			botApi.execute(msg);
		}
	}

	private void handleHolderNameInput(String text, Long chatId, Long userId, DialogState state, BotApi botApi) throws Exception {
		String holderName = text.isEmpty() ? null : text;
		DialogState newState = new DialogState("label", state.cardNumber, state.expiryMonth, state.expiryYear, holderName);
		stateService.setState(userId, "label", newState);

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.addCardLabel());
		botApi.execute(msg);
	}

	private void handleLabelInput(String text, Long chatId, Long userId, DialogState state, BotApi botApi) throws Exception {
		String label = text.isEmpty() ? null : text;
		DialogState newState = new DialogState("category_confirm", state.cardNumber, state.expiryMonth, state.expiryYear, state.holderName, label);
		stateService.setState(userId, "category_confirm", newState);

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.selectCategory());
		msg.setReplyMarkup(keyboards.categoryButtons(cardService.getAllCategories()));
		botApi.execute(msg);
	}

	private void handleCards(Long chatId, Long userId, BotApi botApi) throws Exception {
		List<Card> cards = cardService.getCardsByUser(userId);

		if (cards.isEmpty()) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.noCards());
			botApi.execute(msg);
			return;
		}

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.cardsList(cards.size()));
		botApi.execute(msg);

		for (Card card : cards) {
			String text = messages.cardItem(card.getCategory().getName(), card.getLast4(), card.getLabel());
			SendMessage cardMsg = new SendMessage(String.valueOf(chatId), text);
			cardMsg.setReplyMarkup(keyboards.cardActionButtons(card.getId().toString()));
			botApi.execute(cardMsg);
		}
	}

	private void handleDeleteStart(Long chatId, Long userId, BotApi botApi) throws Exception {
		List<Card> cards = cardService.getCardsByUser(userId);

		if (cards.isEmpty()) {
			SendMessage msg = new SendMessage(String.valueOf(chatId), messages.noCards());
			botApi.execute(msg);
			return;
		}

		DialogState state = new DialogState("delete_confirm", null);
		stateService.setState(userId, "delete_confirm", state);

		SendMessage msg = new SendMessage(String.valueOf(chatId), "O'chirilacak kartani tanlang:");
		botApi.execute(msg);

		for (Card card : cards) {
			String text = messages.cardItem(card.getCategory().getName(), card.getLast4(), card.getLabel());
			SendMessage cardMsg = new SendMessage(String.valueOf(chatId), text);
			cardMsg.setReplyMarkup(keyboards.cardActionButtons(card.getId().toString()));
			botApi.execute(cardMsg);
		}
	}

	private void handleCancel(Long chatId, Long userId, BotApi botApi) throws Exception {
		stateService.clearState(userId);

		SendMessage msg = new SendMessage(String.valueOf(chatId), messages.operationCancelled());
		msg.setReplyMarkup(new ReplyKeyboardRemove(true));
		botApi.execute(msg);
	}

	private void handleEditLabelInput(String text, Long chatId, Long userId, DialogState state, BotApi botApi) throws Exception {
		String newLabel = text.isEmpty() ? null : text;
		// This will be handled in callback handler for now
		handleCancel(chatId, userId, botApi);
	}

	private void ensureUserExists(Long userId) {
		Optional<User> user = userRepository.findById(userId);
		if (user.isEmpty()) {
			User newUser = new User();
			newUser.setTelegramId(userId);
			newUser.setCreatedAt(LocalDateTime.now());
			userRepository.save(newUser);
		}
	}

	public static class DialogState {
		public String step;
		public String cardNumber;
		public Integer expiryMonth;
		public Integer expiryYear;
		public String holderName;
		public String label;

		public DialogState() {}

		public DialogState(String step, String cardNumber) {
			this.step = step;
			this.cardNumber = cardNumber;
		}

		public DialogState(String step, String cardNumber, Integer expiryMonth, Integer expiryYear, String holderName) {
			this.step = step;
			this.cardNumber = cardNumber;
			this.expiryMonth = expiryMonth;
			this.expiryYear = expiryYear;
			this.holderName = holderName;
		}

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
