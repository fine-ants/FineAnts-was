package co.fineants.stock.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import joptsimple.internal.Strings;

class ActiveStockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private ActiveStockService service;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void markStockAsActive() {
		// given
		String tickerSymbol = "005930";
		// when
		service.markStockAsActive(tickerSymbol);
		// then
		Double score = redisTemplate.opsForZSet().score(ActiveStockService.ACTIVE_STOCKS_KEY, tickerSymbol);
		Assertions.assertThat(score).isGreaterThan(0);
	}

	@Test
	void markStockAsActive_whenTickerSymbolIsNull_thenDoNothing() {
		// when
		service.markStockAsActive(null);
		service.markStockAsActive(Strings.EMPTY);
		// then
		Long size = redisTemplate.opsForZSet().size(ActiveStockService.ACTIVE_STOCKS_KEY);
		Assertions.assertThat(size).isZero();
	}
}
