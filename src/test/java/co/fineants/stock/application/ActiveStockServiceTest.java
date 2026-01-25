package co.fineants.stock.application;

import java.time.Clock;
import java.util.Collections;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import co.fineants.AbstractContainerBaseTest;

class ActiveStockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private ActiveStockService service;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private Clock spyClock;

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

	@DisplayName("활성 종목이 없으면 빈 집합을 반환한다.")
	@Test
	void getActiveStockTickerSymbols_whenMinutesAgoIsZero_thenReturnEmptySet() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1000000L) // 현재 시간
			.willReturn(1000000L) // 현재 시간
			.willReturn(1000000L + 1L); // 1밀리초 후
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

	@DisplayName("여러 종목을 활성 상태로 표시할 수 있다.")
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
		Long size = redisTemplate.opsForZSet().size(ActiveStockService.ACTIVE_STOCKS_KEY);
		Assertions.assertThat(size).isEqualTo(tickerSymbols.size());
	}

	@DisplayName("컬렉션이 유효하지 않으면 활성 종목 등록 작업은 아무 작업도 수행하지 않는다.")
	@Test
	void markStocksAsActive_whenCollectionsIsInvalid_thenDoNothing() {
		// given
		// when
		service.markStocksAsActive(null);
		service.markStocksAsActive(Collections.emptySet());
		// then
		Long size = redisTemplate.opsForZSet().size(ActiveStockService.ACTIVE_STOCKS_KEY);
		Assertions.assertThat(size).isZero();
	}

	@DisplayName("컬렉션의 요소가 빈 문자열이면 활성 종목 등록 작업은 아무 작업도 수행하지 않는다.")
	@Test
	void markStocksAsActive_whenCollectionElementIsEmpty_thenDoNothing() {
		// given
		Set<String> tickerSymbols = Set.of("", " ", "  ");
		// when
		service.markStocksAsActive(tickerSymbols);
		// then
		Long size = redisTemplate.opsForZSet().size(ActiveStockService.ACTIVE_STOCKS_KEY);
		Assertions.assertThat(size).isZero();
	}
}
