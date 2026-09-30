package uz.card.vault.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {
	@Id
	@Column(columnDefinition = "UUID")
	private UUID id;

	@Column(name = "owner_telegram_id", nullable = false)
	private Long ownerTelegramId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "category_id", nullable = false)
	private CardCategory category;

	@Column(name = "card_number_encrypted", nullable = false)
	private byte[] cardNumberEncrypted;

	@Column(name = "card_number_hash", nullable = false)
	private String cardNumberHash;

	@Column(name = "last4", nullable = false)
	private String last4;

	@Column(name = "expiry_month", nullable = false)
	private Integer expiryMonth;

	@Column(name = "expiry_year", nullable = false)
	private Integer expiryYear;

	@Column(name = "holder_name")
	private String holderName;

	@Column(name = "label")
	private String label;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PrePersist
	protected void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
