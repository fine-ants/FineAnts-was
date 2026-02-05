package co.fineants.api.domain.stock_target_price.domain.entity;

import java.time.LocalDateTime;

import co.fineants.api.domain.BaseEntity;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.PriceRepository;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "target_price_notification")
public class TargetPriceNotification extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money targetPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stock_target_price_id")
	private StockTargetPrice stockTargetPrice;

	private TargetPriceNotification(LocalDateTime createAt, LocalDateTime modifiedAt, Long id,
		Money targetPrice, StockTargetPrice stockTargetPrice) {
		super(createAt, modifiedAt);
		this.id = id;
		this.targetPrice = targetPrice;
		this.stockTargetPrice = stockTargetPrice;
	}

	public static TargetPriceNotification newTargetPriceNotification(Money targetPrice,
		StockTargetPrice stockTargetPrice) {
		return newTargetPriceNotification(null, targetPrice, stockTargetPrice);
	}

	public static TargetPriceNotification newTargetPriceNotification(Long id, Money targetPrice,
		StockTargetPrice stockTargetPrice) {
		return new TargetPriceNotification(LocalDateTime.now(), null, id, targetPrice, stockTargetPrice);
	}

	public String getReferenceId() {
		return stockTargetPrice.getStock().getTickerSymbol();
	}

	public boolean isActive() {
		return stockTargetPrice.getIsActive();
	}

	public boolean isSameTargetPrice(PriceRepository priceRepository) {
		Money currentPrice = priceRepository.fetchPriceBy(stockTargetPrice.getStock().getTickerSymbol())
			.map(CurrentPriceRedisEntity::getPriceMoney)
			.orElseGet(Money::zero);
		return targetPrice.compareTo(currentPrice) == 0;
	}

	public boolean hasAuthorization(Long memberId) {
		return stockTargetPrice.hasAuthorization(memberId);
	}
}
