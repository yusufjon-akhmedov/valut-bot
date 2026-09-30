package uz.card.vault.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import uz.card.vault.domain.Card;
import uz.card.vault.service.CardService;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InlineQueryHandler {

	private final CardService cardService;

	public void handle(InlineQuery query, Long userId, BotApi botApi) throws Exception {
		List<Card> userCards = cardService.getCardsByUser(userId);
		String queryText = query.getQuery().toLowerCase();

		List<InlineQueryResult> results = new ArrayList<>();

		for (Card card : userCards) {
			if (!matchesQuery(card, queryText)) {
				continue;
			}

			String fullNumber = cardService.decryptCardNumber(card, userId);
			String expiry = String.format("%02d/%02d", card.getExpiryMonth(), card.getExpiryYear() % 100);
			String holderName = card.getHolderName() != null ? card.getHolderName() : "N/A";

			String messageText = String.format(
				"💳 %s\n```\n%s\n```\n📅 %s\n👤 %s",
				card.getCategory().getName(),
				fullNumber,
				expiry,
				holderName);

			InlineQueryResultArticle result = new InlineQueryResultArticle();
			result.setId(card.getId().toString());
			result.setTitle(card.getCategory().getName() + " (" + card.getLast4() + ")");
			result.setDescription(card.getLabel() != null ? card.getLabel() : "Karta");

			InputTextMessageContent content = new InputTextMessageContent();
			content.setMessageText(messageText);
			content.setParseMode("Markdown");
			result.setInputMessageContent(content);

			results.add(result);

			if (results.size() >= 50) {
				break;
			}
		}

		AnswerInlineQuery answer = new AnswerInlineQuery();
		answer.setInlineQueryId(query.getId());
		answer.setResults(results);
		answer.setIsPersonal(true);
		answer.setCacheTime(0);

		botApi.execute(answer);
	}

	private boolean matchesQuery(Card card, String query) {
		if (query.isEmpty()) {
			return true;
		}

		String category = card.getCategory().getCode().toLowerCase();
		String last4 = card.getLast4();
		String label = card.getLabel() != null ? card.getLabel().toLowerCase() : "";

		return category.contains(query) || last4.contains(query) || label.contains(query);
	}
}
