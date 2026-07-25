package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import reactor.core.publisher.Mono;

class CurrentPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceService service;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@Autowired
	private KisService kisService;

	@Autowired
	private Clock spyClock;

	@Value("${stock.current-price.freshness-threshold-millis:5000}")
	private long freshnessThresholdMillis;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@Autowired
	private HolidayService holidayService;

	@BeforeEach
	void setUp() {
		BDDMockito.given(spyLocalDateTimeService.getLocalDateTimeWithNow())
			.willReturn(LocalDateTime.of(2026, 2, 12, 9, 0)); // 목요일
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 현재가가 없어서 동기적 이벤트를 발행하고, 외부 API에서 조회한 현재가를 반환한다.")
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
		CurrentPriceRedisEntity actual = currentPriceRepository.fetchPriceBy(tickerSymbol).orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", freshPrice);
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 종목 현재가가 신선도(freshness) 기준에 맞지 않아서 비동기적 이벤트를 발행하고, 기존 현재가를 반환해야 한다.")
	@Test
	void fetchPrice_whenCurrentPriceIsStale_thenPublishStockCurrentPriceRefreshEventAndReturnStaleCurrentPrice() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);

		String tickerSymbol = "005930";
		long stalePrice = 45000L;
		long freshPrice = 50000L;

		currentPriceRepository.savePrice(tickerSymbol, stalePrice);
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
				Assertions.assertThat(currentPriceRepository.fetchPriceBy(tickerSymbol).orElseThrow())
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

	@DisplayName("종목 현재가 조회 - 상장 폐지된 종목을 대상으로 조회시 0원을 반환한다")
	@Test
	void fetchPrice_whenDelistedStock_thenReturnZeroWon() {
		// given
		String tickerSymbol = "999999"; // 상장 폐지된 종목
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.empty(tickerSymbol)));

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(0L));
		CurrentPriceRedisEntity actual = currentPriceRepository.fetchPriceBy(tickerSymbol).orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", 0L);
	}

	@DisplayName("종목 현재가 조회 - 장시간 외에서 데이터가 존재하지 않으면 동기 이벤트로 조회후 반환한다")
	@Test
	void fetchPrice_whenMarketIsCloseAndCurrentPriceIsNotExist_thenPublishEventAndReturnCurrentPrice() {
		// given
		String tickerSymbol = "005930";
		long freshPrice = 50000L;
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, freshPrice)));

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(freshPrice));
	}
}
