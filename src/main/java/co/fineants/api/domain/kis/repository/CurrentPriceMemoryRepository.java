package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.stock.domain.Stock;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CurrentPriceMemoryRepository implements PriceRepository {

	private final Map<String, CurrentPriceRedisEntity> store;
	private final Clock clock;
	private final long freshnessThresholdMillis;

	public CurrentPriceMemoryRepository(Clock clock,
		@Value("${stock.current-price.freshness-threshold-millis:300000}") long freshnessThresholdMillis) {
		this.store = new ConcurrentHashMap<>();
		this.clock = clock;
		this.freshnessThresholdMillis = freshnessThresholdMillis;
	}

	@Override
	public void savePrice(KisCurrentPrice... prices) {
		Arrays.stream(prices).forEach(this::savePrice);
	}

	private void savePrice(KisCurrentPrice price) {
		savePrice(price.getTickerSymbol(), price.getPrice());
	}

	@Override
	public void savePrice(Stock stock, long price) {
		savePrice(stock.getTickerSymbol(), price);
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		CurrentPriceRedisEntity entity = CurrentPriceRedisEntity.of(tickerSymbol, price, clock.millis());
		store.put(tickerSymbol, entity);
	}

	@Override
	public Optional<CurrentPriceRedisEntity> fetchPriceBy(String tickerSymbol) {
		if (isBlankTickerSymbol(tickerSymbol)) {
			log.warn("tickerSymbol is blank, tickerSymbol: {}", tickerSymbol);
			return Optional.empty();
		}
		if (!store.containsKey(tickerSymbol)) {
			return Optional.empty();
		}
		return Optional.of(store.get(tickerSymbol));
	}

	private boolean isBlankTickerSymbol(String tickerSymbol) {
		return Strings.isBlank(tickerSymbol);
	}
}
