package co.fineants.stock.application;

import java.time.Clock;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveStockService {
	public static final String ACTIVE_STOCKS_KEY = "active_stocks";
	private final StringRedisTemplate template;
	private final Clock clock;

	/**
	 * 사용자가 종목을 조회하거나 포트폴리오를 확인할 때 호출합니다.
	 * 종목 코드의 score를 현재 시간(ms)으로 업데이트합니다.
	 */
	public void markStockAsActive(String tickerSymbol) {
		if (Strings.isEmpty(tickerSymbol)) {
			log.warn("Ticker symbol is blank. Skipping marking stock as active.");
			return;
		}
		try {
			// ZADD active_stocks <current_time> <tickerSymbol>
			template.opsForZSet().add(ACTIVE_STOCKS_KEY, tickerSymbol, clock.millis());
		} catch (IllegalArgumentException e) {
			log.warn("Failed to mark stock as active for ticker symbol '{}': {}", tickerSymbol, e.getMessage());
		}
	}

	/**
	 * 여러 종목을 한 번에 활성 종목으로 등록합니다.
	 * @param tickerSymbols 종목 코드 컬렉션
	 */
	public void markStocksAsActive(Collection<String> tickerSymbols) {
		if (tickerSymbols == null || tickerSymbols.isEmpty()) {
			log.warn("Ticker symbols collection is null or empty. Skipping marking stocks as active.");
			return;
		}
		long currentTime = clock.millis();
		template.executePipelined((RedisCallback<?>)connection -> {
			tickerSymbols.stream()
				.filter(Strings::isNotBlank)
				.forEach(symbol ->
					connection.zSetCommands().zAdd(
						ACTIVE_STOCKS_KEY.getBytes(),
						currentTime,
						symbol.getBytes()
					));
			return null;
		});
	}

	/**
	 * 최근 N분 동안 활동이 있었던 종목 리스트를 가져옵니다.
	 */
	public Set<String> getActiveStockTickerSymbols(long minutesAgo) {
		long threshold = clock.millis() - (minutesAgo * 60 * 1000);

		// ZRANGEBYSCORE active_stocks <threshold> <infinity>
		return template.opsForZSet().rangeByScore(ACTIVE_STOCKS_KEY, threshold, Double.MAX_VALUE);
	}

	/**
	 * 너무 오래된(예: 1시간 이상) 활동 기록은 Redis 메모리 관리를 위해 삭제합니다.
	 */
	public void cleanupInactiveStocks(long minutesAgo) {
		long threshold = clock.millis() - (minutesAgo * 60 * 1000);

		// ZREMRANGEBYSCORE active_stocks 0 <threshold>
		template.opsForZSet().removeRangeByScore(ACTIVE_STOCKS_KEY, 0, threshold);
	}
}
