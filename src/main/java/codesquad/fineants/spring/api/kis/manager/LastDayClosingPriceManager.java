package codesquad.fineants.spring.api.kis.manager;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import codesquad.fineants.spring.api.kis.response.KisClosingPrice;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class LastDayClosingPriceManager {

	private static final String format = "lastDayClosingPrice:%s";
	private final RedisTemplate<String, String> redisTemplate;

	public void addPrice(String tickerSymbol, long price) {
		redisTemplate.opsForValue().set(String.format(format, tickerSymbol), String.valueOf(price), Duration.ofDays(2));
	}

	public void addPrice(KisClosingPrice price) {
		redisTemplate.opsForValue()
			.set(String.format(format, price.getTickerSymbol()), String.valueOf(price.getPrice()), Duration.ofDays(2));
	}

	public Optional<Long> getPrice(String tickerSymbol) {
		String closingPrice = redisTemplate.opsForValue().get(String.format(format, tickerSymbol));
		if (closingPrice == null) {
			return Optional.empty();
		}
		return Optional.of(Long.valueOf(closingPrice));
	}
}
