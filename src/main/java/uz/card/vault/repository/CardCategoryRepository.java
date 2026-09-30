package uz.card.vault.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.card.vault.domain.CardCategory;

import java.util.Optional;

public interface CardCategoryRepository extends JpaRepository<CardCategory, Integer> {
	Optional<CardCategory> findByCode(String code);
}
