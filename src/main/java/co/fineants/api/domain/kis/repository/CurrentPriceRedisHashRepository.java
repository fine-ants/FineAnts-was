package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class CurrentPriceRedisHashRepository implements PriceRepository {

	public static final String KEY = "current_prices";
	private final StringRedisTemplate template;

	@Override
	public void savePrice(KisCurrentPrice... currentPrices) {
		template.opsForHash().putAll(KEY,
			java.util.Arrays.stream(currentPrices)
				.collect(
					java.util.stream.Collectors.toMap(
						KisCurrentPrice::getTickerSymbol,
						cp -> String.valueOf(cp.getPrice())
					)
				)
		);
	}

	@Override
	public void savePrice(Stock stock, long price) {
		template.opsForHash().put(KEY, stock.getTickerSymbol(), String.valueOf(price));
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		template.opsForHash().put(KEY, tickerSymbol, String.valueOf(price));
	}

	@Override
	public Optional<Money> fetchPriceBy(String tickerSymbol) {
		return getCachedPrice(tickerSymbol);
	}

	@Override
	public Optional<Money> fetchPriceBy(PortfolioHolding holding) {
		return holding.fetchPrice(this);
	}

	@Override
	public Optional<Money> getCachedPrice(String tickerSymbol) {
		if (Strings.isBlank(tickerSymbol)) {
			log.warn("tickerSymbol is blank");
			return Optional.empty();
		}
		Object value = template.opsForHash().get(KEY, tickerSymbol);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(Money.won((String)value));
	}
}
