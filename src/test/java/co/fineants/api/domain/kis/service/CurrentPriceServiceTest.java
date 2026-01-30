package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.PriceRepository;
import reactor.core.publisher.Mono;

class CurrentPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceService service;

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private KisService kisService;

	@Autowired
	private Clock spyClock;

	@Value("${stock.current-price.freshness-threshold-millis:5000}")
	private long freshnessThresholdMillis;

	@DisplayName("종목 현재가 조회 - 캐시된 종목 현재가를 반환한다")
	@Test
	void fetchPrice_whenCurrentPriceIsFresh_thenReturnCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		long expectedPrice = 50000L;
		priceRepository.savePrice(tickerSymbol, expectedPrice);

		// when
		Money price = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(price).isEqualTo(Money.won(50000L));
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 현재가가 없어서 동기적 이벤트를 발행하고, 외부 API에서 현재가를 조회하여 반환한다.")
	@Test
	void fetchPrice_whenPriceNotInCache_thenPublishStockCurrentPriceRefreshSyncEventAndReturnClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long freshPrice = 50000L;
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, freshPrice)));
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(freshPrice));
		CurrentPriceRedisEntity actual = priceRepository.fetchPriceBy(tickerSymbol).orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", freshPrice);
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 존재하는 현재가가 신선도(freshness) 기준에 맞지 않으면 비동기적 이벤트를 발행하고, 캐시된 현재가를 반환한다.")
	@Test
	void fetchPrice_whenCurrentPriceIsStale_thenPublishStockCurrentPriceRefreshEventAndReturnStaleCurrentPrice() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);

		String tickerSymbol = "005930";
		long stalePrice = 45000L;
		long freshPrice = 50000L;

		priceRepository.savePrice(tickerSymbol, stalePrice);
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, freshPrice)));

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
		// then : 비동기 캐시 업데이트 검증 (최대 2초 대기)
		Awaitility.await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				Assertions.assertThat(priceRepository.fetchPriceBy(tickerSymbol).orElseThrow())
					.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
					.hasFieldOrPropertyWithValue("price", freshPrice));
	}

	@DisplayName("종목 현재가 조회 - 외부 API 호출 실패 시 예외를 던진다")
	@Test
	void fetchPrice_whenExternalAPIFails_thenThrowException() {
		// given
		String tickerSymbol = "005930";
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.empty());

		// when
		Throwable throwable = Assertions.catchThrowable(() -> service.fetchPrice(tickerSymbol));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Failed to fetch current price for " + tickerSymbol);
	}
}
