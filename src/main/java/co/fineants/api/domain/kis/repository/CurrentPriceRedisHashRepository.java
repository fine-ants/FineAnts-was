package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class CurrentPriceRedisHashRepository implements PriceRepository {

	public static final String KEY = "current_prices";
	private final StringRedisTemplate template;
	private final ObjectMapper objectMapper;

	@Override
	public void savePrice(KisCurrentPrice... currentPrices) {
		if (currentPrices == null || currentPrices.length == 0) {
			log.warn("currentPrices is null or empty");
			return;
		}
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
		savePrice(stock.getTickerSymbol(), price);
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		if (Strings.isBlank(tickerSymbol)) {
			log.warn("tickerSymbol is blank");
			return;
		}
		if (price < 0) {
			log.warn("price is negative: {}", price);
			return;
		}
		CurrentPriceRedisEntity entity = CurrentPriceRedisEntity.now(tickerSymbol, price);
		template.opsForHash().put(KEY, tickerSymbol, toJson(entity));
	}

	private String toJson(CurrentPriceRedisEntity entity) {
		try {
			return objectMapper.writeValueAsString(entity);
		} catch (Exception e) {
			log.error("Failed to serialize CurrentPriceRedisEntity to JSON", e);
			throw new IllegalArgumentException("Serialization error", e);
		}
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
		CurrentPriceRedisEntity entity = fromJson((String)value);
		return Optional.of(Money.won(entity.getPrice()));
	}

	private CurrentPriceRedisEntity fromJson(String json) {
		try {
			return objectMapper.readValue(json, CurrentPriceRedisEntity.class);
		} catch (Exception e) {
			log.error("Failed to deserialize JSON to CurrentPriceRedisEntity", e);
			throw new IllegalArgumentException("Deserialization error", e);
		}
	}
}
