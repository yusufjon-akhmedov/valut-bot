package uz.card.vault.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.card.vault.domain.ConversationState;

import java.util.Optional;

public interface ConversationStateRepository extends JpaRepository<ConversationState, Integer> {
	Optional<ConversationState> findByUserTelegramId(Long userTelegramId);
}
