package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClosingPriceRedisHashRepository implements ClosingPriceRepository {

	public static final String KEY = "closing_prices";
	private final StringRedisTemplate template;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Override
	public void savePrice(KisClosingPrice price) {
		savePrice(price.getTickerSymbol(), price.getPrice());
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		if (isBlankTickerSymbol(tickerSymbol)) {
			log.warn("tickerSymbol is blank, tickerSymbol: {}", tickerSymbol);
			return;
		}
		if (price < 0) {
			log.warn("price is negative, tickerSymbol: {}, price: {}", tickerSymbol, price);
			return;
		}
		ClosingPriceRedisEntity entity = ClosingPriceRedisEntity.of(tickerSymbol, price, clock.millis());
		template.opsForHash().put(KEY, tickerSymbol, toJson(entity));
	}

	private String toJson(ClosingPriceRedisEntity entity) {
		try {
			return objectMapper.writeValueAsString(entity);
		} catch (Exception e) {
			log.error("Failed to serialize ClosingPriceRedisEntity to JSON", e);
			throw new IllegalArgumentException("Serialization error", e);
		}
	}

	@Override
	public Optional<ClosingPriceRedisEntity> fetchPrice(String tickerSymbol) {
		if (isBlankTickerSymbol(tickerSymbol)) {
			log.warn("tickerSymbol is blank, tickerSymbol: {}", tickerSymbol);
			return Optional.empty();
		}
		Object value = template.opsForHash().get(KEY, tickerSymbol);
		if (value == null) {
			return Optional.empty();
		}
		ClosingPriceRedisEntity entity = fromJson((String)value);
		return Optional.of(entity);
	}

	private boolean isBlankTickerSymbol(String tickerSymbol) {
		return Strings.isBlank(tickerSymbol);
	}

	private ClosingPriceRedisEntity fromJson(String json) {
		try {
			return objectMapper.readValue(json, ClosingPriceRedisEntity.class);
		} catch (Exception e) {
			log.error("Failed to deserialize JSON to ClosingPriceRedisEntity", e);
			throw new IllegalArgumentException("Deserialization error", e);
		}
	}
}
