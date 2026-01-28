package co.fineants.api.domain.stock_target_price.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class TargetPriceNotificationSearchItem {
	private String companyName;
	private String tickerSymbol;
	private Money lastPrice;
	private List<TargetPriceItem> targetPrices;
	private Boolean isActive;
	private LocalDateTime lastUpdated;

	public static TargetPriceNotificationSearchItem from(StockTargetPrice stockTargetPrice,
		ClosingPriceRepository repository) {

		List<TargetPriceItem> targetPrices = stockTargetPrice.getTargetPriceNotifications().stream()
			.map(TargetPriceItem::from)
			.toList();

		return TargetPriceNotificationSearchItem.builder()
			.companyName(stockTargetPrice.getStock().getCompanyName())
			.tickerSymbol(stockTargetPrice.getStock().getTickerSymbol())
			.lastPrice(repository.fetchPrice(stockTargetPrice.getStock().getTickerSymbol())
				.map(ClosingPriceRedisEntity::getPriceMoney)
				.orElse(Money.zero()))
			.targetPrices(targetPrices)
			.isActive(stockTargetPrice.getIsActive())
			.lastUpdated(stockTargetPrice.getModifiedAt())
			.build();
	}
}
