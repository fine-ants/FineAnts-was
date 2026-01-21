package co.fineants.stock.application;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActiveStockService {
	public static final String ACTIVE_STOCKS_KEY = "active_stocks";
	private final StringRedisTemplate template;

	/**
	 * 사용자가 종목을 조회하거나 포트폴리오를 확인할 때 호출합니다.
	 * 종목 코드의 score를 현재 시간(ms)으로 업데이트합니다.
	 */
	public void markStockAsActive(String tickerSymbol) {
		// ZADD active_stocks <current_time> <tickerSymbol>
		template.opsForZSet().add(ACTIVE_STOCKS_KEY, tickerSymbol, System.currentTimeMillis());
	}
}
