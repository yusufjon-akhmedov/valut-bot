package uz.card.vault.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.card.vault.domain.ConversationState;
import uz.card.vault.repository.ConversationStateRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class ConversationStateService {

	private final ConversationStateRepository repository;
	private final ObjectMapper objectMapper;

	public ConversationStateService(ConversationStateRepository repository) {
		this.repository = repository;
		this.objectMapper = new ObjectMapper();
	}

	public void setState(Long userTelegramId, String state, Object data) {
		Optional<ConversationState> existing = repository.findByUserTelegramId(userTelegramId);
		ConversationState conversationState;

		if (existing.isPresent()) {
			conversationState = existing.get();
		} else {
			conversationState = new ConversationState();
			conversationState.setUserTelegramId(userTelegramId);
			conversationState.setCreatedAt(LocalDateTime.now());
		}

		conversationState.setState(state);
		if (data != null) {
			try {
				conversationState.setData(objectMapper.writeValueAsString(data));
			} catch (Exception e) {
				throw new RuntimeException("Failed to serialize state data", e);
			}
		}
		conversationState.setUpdatedAt(LocalDateTime.now());
		repository.save(conversationState);
	}

	@Transactional(readOnly = true)
	public Optional<ConversationState> getState(Long userTelegramId) {
		return repository.findByUserTelegramId(userTelegramId);
	}

	@Transactional(readOnly = true)
	public <T> Optional<T> getStateData(Long userTelegramId, Class<T> dataType) {
		Optional<ConversationState> state = repository.findByUserTelegramId(userTelegramId);
		if (state.isPresent() && state.get().getData() != null) {
			try {
				return Optional.of(objectMapper.readValue(state.get().getData(), dataType));
			} catch (Exception e) {
				log.warn("Failed to deserialize state data for user {}", userTelegramId, e);
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	public void clearState(Long userTelegramId) {
		repository.findByUserTelegramId(userTelegramId).ifPresent(repository::delete);
	}
}
