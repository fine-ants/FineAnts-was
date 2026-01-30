package co.fineants.api.domain.kis.service;

import java.time.Clock;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import reactor.core.publisher.Mono;

class ClosingPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private ClosingPriceService closingPriceService;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private KisService kisService;

	@Autowired
	private Clock spyClock;

	@Value("${stock.closing-price.freshness-threshold-millis:86400000}")
	private long freshnessThresholdMillis;

	@DisplayName("종목 종가 조회 - 캐시된 종목 종가 데이터 조회한다")
	@Test
	void fetchPrice_whenCachedClosingPriceExist_thenReturnClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long closingPrice = 60000L;
		closingPriceRepository.savePrice(tickerSymbol, closingPrice);

		// when
		Money actual = closingPriceService.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actual).isEqualTo(Money.won(closingPrice));
	}

	@DisplayName("종목 종가 조회 - 캐시된 종목 종가 데이터가 없으면 외부 API를 호출하여 종가 데이터를 반환한다")
	@Test
	void fetchPrice_whenCachedClosingPriceNotExist_thenReturnClosingPriceFromExternalAPI() {
		// given
		String tickerSymbol = "005930";
		long freshPrice = 60000L;
		BDDMockito.given(kisService.fetchClosingPrice(tickerSymbol))
			.willReturn(Mono.just(KisClosingPrice.create(tickerSymbol, freshPrice)));

		// when
		Money actual = closingPriceService.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actual).isEqualTo(Money.won(freshPrice));
	}

	@DisplayName("종목 종가 조회 - 외부 API 호출 실패 시 예외를 던진다")
	@Test
	void fetchPrice_whenExternalAPIFails_thenThrowException() {
		// given
		String tickerSymbol = "005930";
		BDDMockito.given(kisService.fetchClosingPrice(tickerSymbol))
			.willReturn(Mono.empty());

		// when
		Throwable throwable = Assertions.catchThrowable(() -> closingPriceService.fetchPrice(tickerSymbol));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Closing price should be available after refresh for " + tickerSymbol);
	}

	@DisplayName("종목 종가 조회 - 신선도가 떨어진 종목 종가 데이터가 있으면 비동기 갱신 이벤트를 발행하고 기존 종가 데이터를 반환한다")
	@Test
	void fetchPrice_whenCachedClosingPriceIsStale_thenPublishStockClosingPriceRefreshEventAndReturnStaleClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long stalePrice = 60000L;
		long freshPrice = 65000L;

		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L); // after freshness threshold
		BDDMockito.given(kisService.fetchClosingPrice(tickerSymbol))
			.willReturn(Mono.just(KisClosingPrice.create(tickerSymbol, freshPrice)));

		closingPriceRepository.savePrice(tickerSymbol, stalePrice);

		// when
		Money actual = closingPriceService.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actual).isEqualTo(Money.won(stalePrice));
		Awaitility.await()
			.atMost(java.time.Duration.ofSeconds(2))
			.untilAsserted(() -> {
				ClosingPriceRedisEntity updatedEntity = closingPriceRepository.fetchPrice(tickerSymbol).orElseThrow();
				Assertions.assertThat(updatedEntity)
					.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
					.hasFieldOrPropertyWithValue("price", freshPrice);
			});
	}
}
