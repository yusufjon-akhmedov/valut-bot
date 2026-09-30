package uz.card.vault.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import uz.card.vault.config.AppProperties;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateRouter {

	private final AppProperties appProperties;
	private final CommandHandler commandHandler;
	private final CallbackHandler callbackHandler;
	private final InlineQueryHandler inlineQueryHandler;
	private final Messages messages;

	public void route(Update update, BotApi botApi) {
		Long userId = extractUserId(update);
		if (userId == null) {
			return;
		}

		if (!appProperties.getTelegram().isUserAllowed(userId)) {
			log.warn("Unauthorized access attempt from user: {}", userId);
			if (update.hasMessage()) {
				try {
					botApi.execute(new SendMessage(
						String.valueOf(update.getMessage().getChatId()),
						messages.accessDenied()));
				} catch (Exception e) {
					log.error("Failed to send access denied message", e);
				}
			}
			return;
		}

		try {
			if (update.hasMessage()) {
				handleMessage(update.getMessage(), userId, botApi);
			} else if (update.hasCallbackQuery()) {
				callbackHandler.handle(update.getCallbackQuery(), userId, botApi);
			} else if (update.hasInlineQuery()) {
				inlineQueryHandler.handle(update.getInlineQuery(), userId, botApi);
			}
		} catch (Exception e) {
			log.error("Error routing update for user {}: {}", userId, e.getMessage(), e);
		}
	}

	private void handleMessage(Message message, Long userId, BotApi botApi) throws Exception {
		if (!message.hasText()) {
			return;
		}

		String text = message.getText().trim();
		Long chatId = message.getChatId();

		if (text.startsWith("/")) {
			String command = text.split("\\s+")[0].toLowerCase();
			String args = text.length() > command.length()
				? text.substring(command.length()).trim()
				: "";
			commandHandler.handle(command, args, chatId, userId, botApi);
		} else {
			// Text input during dialog
			commandHandler.handleTextInput(text, chatId, userId, botApi);
		}
	}

	private Long extractUserId(Update update) {
		if (update.hasMessage()) {
			return update.getMessage().getFrom().getId();
		} else if (update.hasCallbackQuery()) {
			return update.getCallbackQuery().getFrom().getId();
		} else if (update.hasInlineQuery()) {
			return update.getInlineQuery().getFrom().getId();
		}
		return null;
	}
}
