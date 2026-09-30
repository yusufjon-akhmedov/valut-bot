package uz.card.vault.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.card.vault.telegram.VaultBot;

@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.telegram.enabled", havingValue = "true")
public class TelegramBotConfig {

	private final VaultBot vaultBot;

	@Bean
	public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		botsApi.registerBot(vaultBot);
		log.info("✅ Telegram bot registered and polling started");
		return botsApi;
	}
}
