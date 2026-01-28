package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.errors.exception.business.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.business.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.business.RequestLimitExceededKisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClosingPriceRedisHashRepository implements ClosingPriceRepository {

	public static final String KEY = "closing_prices";
	private static final String CLOSING_PRICE_FORMAT = "lastDayClosingPrice:%s";
	private final StringRedisTemplate template;
	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	@Override
	public void savePrice(String tickerSymbol, long price) {
		ClosingPriceRedisEntity entity = ClosingPriceRedisEntity.of(tickerSymbol, price, clock.millis());
		template.opsForHash().put(KEY, tickerSymbol, toJson(entity));
	}

	@Override
	public void savePrice(KisClosingPrice price) {
		savePrice(price.getTickerSymbol(), price.getPrice());
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

	private Optional<String> getCachedPrice(String tickerSymbol) {
		return Optional.ofNullable(template.opsForValue().get(String.format(CLOSING_PRICE_FORMAT, tickerSymbol)));
	}

	private ClosingPriceRedisEntity fromJson(String json) {
		try {
			return objectMapper.readValue(json, ClosingPriceRedisEntity.class);
		} catch (Exception e) {
			log.error("Failed to deserialize JSON to ClosingPriceRedisEntity", e);
			throw new IllegalArgumentException("Deserialization error", e);
		}
	}

	private Optional<KisClosingPrice> fetchClosingPriceFromKis(String tickerSymbol) {
		return kisClient.fetchClosingPrice(tickerSymbol)
			.doOnSuccess(price -> log.debug("reload stock current price {}", price))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout());
	}
}
