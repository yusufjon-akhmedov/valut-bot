package uz.card.vault.telegram;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

public interface BotApi {
	<T extends Serializable> T execute(BotApiMethod<T> method) throws TelegramApiException;

	void executeAsync(BotApiMethod<?> method);
}
