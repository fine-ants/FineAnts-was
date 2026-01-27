package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.errors.exception.business.CredentialsTypeKisException;
import co.fineants.api.global.errors.exception.business.ExpiredAccessTokenKisException;
import co.fineants.api.global.errors.exception.business.RequestLimitExceededKisException;
import co.fineants.stock.domain.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Component
@Slf4j
public class CurrentPriceRedisRepository implements PriceRepository {
	private static final String CURRENT_PRICE_FORMAT = "cp:%s";
	private final RedisTemplate<String, String> redisTemplate;
	private final KisClient kisClient;
	private final DelayManager delayManager;
	private final Clock clock;

	@Override
	public void savePrice(KisCurrentPrice... currentPrices) {
		Arrays.stream(currentPrices).forEach(this::savePrice);
	}

	private KisCurrentPrice savePrice(KisCurrentPrice currentPrice) {
		savePrice(currentPrice.getTickerSymbol(), currentPrice.getPrice());
		return currentPrice;
	}

	@Override
	public void savePrice(Stock stock, long price) {
		savePrice(stock.getTickerSymbol(), price);
	}

	@Override
	public void savePrice(String tickerSymbol, long price) {
		redisTemplate.opsForValue().set(CURRENT_PRICE_FORMAT.formatted(tickerSymbol), String.valueOf(price));
	}

	@Override
	public Optional<CurrentPriceRedisEntity> fetchPriceBy(String tickerSymbol) {
		if (isBlankTickerSymbol(tickerSymbol)) {
			log.warn("tickerSymbol is blank, tickerSymbol: {}", tickerSymbol);
			return Optional.empty();
		}
		String value = redisTemplate.opsForValue().get(String.format(CURRENT_PRICE_FORMAT, tickerSymbol));
		if (value == null) {
			Optional<KisCurrentPrice> kisCurrentPrice = fetchAndCachePriceFromKis(tickerSymbol);
			return kisCurrentPrice
				.map(price -> CurrentPriceRedisEntity.of(price.getTickerSymbol(), price.getPrice(), clock.millis()));
		}
		return Optional.of(
			CurrentPriceRedisEntity.of(tickerSymbol, Long.parseLong(value), clock.millis()));
	}

	private boolean isBlankTickerSymbol(String tickerSymbol) {
		return Strings.isBlank(tickerSymbol);
	}

	private Optional<KisCurrentPrice> fetchAndCachePriceFromKis(String tickerSymbol) {
		return kisClient.fetchCurrentPrice(tickerSymbol)
			.doOnSuccess(kisCurrentPrice -> log.debug("reload stock current price {}", kisCurrentPrice))
			.onErrorResume(ExpiredAccessTokenKisException.class::isInstance, throwable -> Mono.empty())
			.onErrorResume(CredentialsTypeKisException.class::isInstance, throwable -> Mono.empty())
			.retryWhen(Retry.fixedDelay(5, delayManager.fixedDelay())
				.filter(RequestLimitExceededKisException.class::isInstance))
			.onErrorResume(Exceptions::isRetryExhausted, throwable -> Mono.empty())
			.blockOptional(delayManager.timeout())
			.map(this::savePrice);
	}

	@Override
	public void clear() {
		redisTemplate.execute((RedisCallback<Object>)connection -> {
			ScanOptions options = ScanOptions.scanOptions()
				.match("cp:*")
				.count(100)
				.build();
			try (Cursor<byte[]> cursor = connection.scan(options)) {
				List<byte[]> keysToDelete = new ArrayList<>();
				while (cursor.hasNext()) {
					keysToDelete.add(cursor.next());
					// Delete in batches of 100 keys
					if (keysToDelete.size() >= 100) {
						connection.del(keysToDelete.toArray(new byte[keysToDelete.size()][]));
						keysToDelete.clear();
					}
				}
				// Delete any remaining keys
				if (!keysToDelete.isEmpty()) {
					connection.del(keysToDelete.toArray(new byte[keysToDelete.size()][]));
				}
			}
			return null;
		});
	}
}
