package codesquad.fineants.spring.api.kis.manager;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class LastDayClosingPriceManager {

	private static final String format = "lastDayClosingPrice:%s";
	private final RedisTemplate<String, String> redisTemplate;

	public void addPrice(String tickerSymbol, long price) {
		redisTemplate.opsForValue().set(String.format(format, tickerSymbol), String.valueOf(price), Duration.ofDays(2));
	}

	public Long getPrice(String tickerSymbol) {
		String price = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (price == null) {
			throw new IllegalArgumentException(String.format("%s 종목에 대한 가격을 찾을 수 없습니다.", tickerSymbol));
		}
		return Long.valueOf(price);
	}

	public boolean hasPrice(String tickerSymbol) {
		String currentPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		return currentPrice != null;
	}
}
