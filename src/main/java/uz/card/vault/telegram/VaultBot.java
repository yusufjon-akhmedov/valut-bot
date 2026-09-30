package uz.card.vault.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.card.vault.config.AppProperties;

import java.io.Serializable;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
public class VaultBot extends TelegramLongPollingBot {

	private final AppProperties appProperties;
	private final UpdateRouter updateRouter;

	@Override
	public String getBotUsername() {
		return "vault_bot";
	}

	@Override
	public String getBotToken() {
		return appProperties.getTelegram().getBotToken();
	}

	@Override
	public void onUpdateReceived(Update update) {
		try {
			BotApiAdapter adapter = new BotApiAdapter(this);
			updateRouter.route(update, adapter);
		} catch (Exception e) {
			log.error("Error processing update: {}", e.getMessage(), e);
		}
	}

	public static class BotApiAdapter implements BotApi {
		private final TelegramLongPollingBot bot;

		public BotApiAdapter(TelegramLongPollingBot bot) {
			this.bot = bot;
		}

		@Override
		public <T extends Serializable> T execute(BotApiMethod<T> method) throws TelegramApiException {
			return bot.execute(method);
		}

		@Override
		public void executeAsync(BotApiMethod<?> method) {
			new Thread(() -> {
				try {
					bot.execute(method);
				} catch (TelegramApiException e) {
					log.debug("Async call result: {}", e.getMessage());
				}
			}).start();
		}
	}
}
