package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.stock.domain.Stock;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CurrentPriceMemoryRepository implements CurrentPriceRepository {

	private final Map<String, CurrentPriceRedisEntity> store;
	private final Clock clock;

	public CurrentPriceMemoryRepository(Clock clock) {
		this.store = new ConcurrentHashMap<>();
		this.clock = clock;
	}

	@Override
	public void savePrice(KisCurrentPrice... prices) {
		Arrays.stream(prices).forEach(price -> savePrice(price.getTickerSymbol(), price.getPrice()));
	}

	@Override
	public void savePrice(Stock stock, long price) {
		savePrice(stock.getTickerSymbol(), price);
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		if (isBlankTickerSymbol(tickerSymbol)) {
			log.warn("tickerSymbol is blank, tickerSymbol: {}", tickerSymbol);
			return;
		}
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

	@Override
	public void clear() {
		store.clear();
	}
}
