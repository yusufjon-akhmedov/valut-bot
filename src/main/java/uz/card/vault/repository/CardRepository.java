package uz.card.vault.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.card.vault.domain.Card;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
	List<Card> findByOwnerTelegramId(Long ownerTelegramId);

	List<Card> findByOwnerTelegramIdOrderByCreatedAtDesc(Long ownerTelegramId);

	@Query("SELECT c FROM Card c WHERE c.ownerTelegramId = :ownerTelegramId AND c.cardNumberHash = :hash")
	Optional<Card> findByOwnerAndHash(@Param("ownerTelegramId") Long ownerTelegramId, @Param("hash") String hash);

	List<Card> findByOwnerTelegramIdAndCategory_IdOrderByCreatedAtDesc(
		Long ownerTelegramId, Integer categoryId);
}
