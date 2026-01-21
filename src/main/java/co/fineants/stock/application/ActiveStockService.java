package co.fineants.stock.application;

import java.util.Set;

import org.apache.logging.log4j.util.Strings;
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
			template.opsForZSet().add(ACTIVE_STOCKS_KEY, tickerSymbol, System.currentTimeMillis());
		} catch (IllegalArgumentException e) {
			log.warn("Ticker symbol is null. Skipping marking stock as active.");
		}
	}

	/**
	 * 최근 N분 동안 활동이 있었던 종목 리스트를 가져옵니다.
	 */
	public Set<String> getActiveStockTickerSymbols(long minutesAgo) {
		long threshold = System.currentTimeMillis() - (minutesAgo * 60 * 1000);

		// ZRANGEBYSCORE active_stocks <threshold> <infinity>
		return template.opsForZSet().rangeByScore(ACTIVE_STOCKS_KEY, threshold, Double.MAX_VALUE);
	}
}
