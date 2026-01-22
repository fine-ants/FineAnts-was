package co.fineants.stock.application;

import java.util.Set;

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

	@Test
	void getActiveStockTickerSymbols_whenMinutesAgoIsOne_thenReturnRecentlyActiveStocks() {
		// given
		String tickerSymbol1 = "005930";
		String tickerSymbol2 = "000660";
		service.markStockAsActive(tickerSymbol1);
		service.markStockAsActive(tickerSymbol2);
		// when
		Set<String> activeStocks = service.getActiveStockTickerSymbols(1); // 1분 이내 활동한 종목 조회
		// then
		Assertions.assertThat(activeStocks)
			.containsExactlyInAnyOrder(tickerSymbol1, tickerSymbol2);
	}

	@Test
	void getActiveStockTickerSymbols_whenMinutesAgoIsZero_thenReturnEmptySet() {
		// given
		String tickerSymbol1 = "005930";
		String tickerSymbol2 = "000660";
		service.markStockAsActive(tickerSymbol1);
		service.markStockAsActive(tickerSymbol2);
		// when
		Set<String> activeStocks = service.getActiveStockTickerSymbols(0); // 0분 이내 활동한 종목 조회
		// then
		Assertions.assertThat(activeStocks).isEmpty();
	}

	@Test
	void getActiveStockTickerSymbols_whenMinutesAgoIsNegative_thenReturnEmptySet() {
		// given
		String tickerSymbol1 = "005930";
		String tickerSymbol2 = "000660";
		service.markStockAsActive(tickerSymbol1);
		service.markStockAsActive(tickerSymbol2);
		// when
		Set<String> activeStocks = service.getActiveStockTickerSymbols(-1); // -1분 이내 활동한 종목 조회
		// then
		Assertions.assertThat(activeStocks).isEmpty();
	}

	@Test
	void cleanupInactiveStocks() {
		// given
		String tickerSymbol1 = "005930";
		String tickerSymbol2 = "000660";
		service.markStockAsActive(tickerSymbol1);
		service.markStockAsActive(tickerSymbol2);
		// when
		service.cleanupInactiveStocks(0); // 0분 이상 활동이 없는 종목 정리
		// then
		Long size = redisTemplate.opsForZSet().size(ActiveStockService.ACTIVE_STOCKS_KEY);
		Assertions.assertThat(size).isZero();
	}

	@Test
	void markStocksAsActive() {
		// given
		Set<String> tickerSymbols = Set.of("005930", "000660", "035420");
		// when
		service.markStocksAsActive(tickerSymbols);
		// then
		for (String symbol : tickerSymbols) {
			Double score = redisTemplate.opsForZSet().score(ActiveStockService.ACTIVE_STOCKS_KEY, symbol);
			Assertions.assertThat(score).isGreaterThan(0);
		}
	}
}
