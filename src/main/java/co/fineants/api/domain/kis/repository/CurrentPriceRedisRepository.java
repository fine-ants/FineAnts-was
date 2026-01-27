package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class CurrentPriceRedisRepository implements PriceRepository {
	private static final String CURRENT_PRICE_FORMAT = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final DelayManager delayManager;
	private final Clock clock;
	private final ObjectMapper objectMapper;

	@Override
	public void savePrice(KisCurrentPrice... currentPrices) {
		Arrays.stream(currentPrices).forEach(this::savePrice);
	}

	private void savePrice(KisCurrentPrice currentPrice) {
		savePrice(currentPrice.getTickerSymbol(), currentPrice.getPrice());
	}

	@Override
	public void savePrice(Stock stock, long price) {
		savePrice(stock.getTickerSymbol(), price);
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		CurrentPriceRedisEntity entity = CurrentPriceRedisEntity.of(tickerSymbol, price, clock.millis());
		redisTemplate.opsForValue().set(CURRENT_PRICE_FORMAT.formatted(tickerSymbol), toJson(entity));
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
	public Optional<CurrentPriceRedisEntity> fetchPriceBy(String tickerSymbol) {
		if (isBlankTickerSymbol(tickerSymbol)) {
			log.warn("tickerSymbol is blank, tickerSymbol: {}", tickerSymbol);
			return Optional.empty();
		}
		String value = redisTemplate.opsForValue().get(String.format(CURRENT_PRICE_FORMAT, tickerSymbol));
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(fromJson(value));
	}

	private boolean isBlankTickerSymbol(String tickerSymbol) {
		return Strings.isBlank(tickerSymbol);
	}

	private CurrentPriceRedisEntity fromJson(String json) {
		try {
			return objectMapper.readValue(json, CurrentPriceRedisEntity.class);
		} catch (Exception e) {
			log.error("Failed to deserialize JSON to CurrentPriceRedisEntity", e);
			throw new IllegalArgumentException("Deserialization error", e);
		}
	}

	@Override
	public void clear() {
		Set<String> keys = redisTemplate.keys("cp:*");
		if (keys == null || keys.isEmpty()) {
			return;
		}
		redisTemplate.delete(keys);
	}
}
