package uz.card.vault.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.card.vault.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
