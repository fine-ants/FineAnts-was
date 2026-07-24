package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ClosingPriceServiceUnitTest {

	private ClosingPriceService closingPriceService;

	@Mock
	private ClosingPriceRepository closingPriceRepository;

	@Mock
	private KisService kisService;

	@Mock
	private Clock clock;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private long freshnessThresholdMillis;

	@BeforeEach
	void setUp() {
		freshnessThresholdMillis = 86400000;
		closingPriceService = new ClosingPriceService(closingPriceRepository, clock, freshnessThresholdMillis,
			eventPublisher);
	}

	@DisplayName("종목 종가 조회 - Redis 캐시된 종목 종가 데이터 조회한다")
	@Test
	void should_return_closing_price_when_closing_price_saved_in_redis() {
		// given
		String tickerSymbol = "005930";
		long closingPrice = 60000L;

		long millis = LocalDate.of(2026, 7, 24).atStartOfDay().toEpochSecond(ZoneOffset.UTC);
		BDDMockito.given(clock.millis())
			.willReturn(millis);
		ClosingPriceRedisEntity entity = ClosingPriceRedisEntity.of(tickerSymbol, closingPrice, millis);
		BDDMockito.given(closingPriceRepository.fetchPrice(tickerSymbol))
			.willReturn(Optional.of(entity));
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

		BDDMockito.given(clock.millis())
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

	@DisplayName("종목 종가 저장 - 종목 종가 데이터를 저장한다")
	@Test
	void savePrice_thenStoreClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long closingPrice = 60000L;

		// when
		closingPriceService.savePrice(tickerSymbol, closingPrice);

		// then
		ClosingPriceRedisEntity entity = closingPriceRepository.fetchPrice(tickerSymbol).orElseThrow();
		Assertions.assertThat(entity)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", closingPrice);
	}

	@DisplayName("종목 종가 저장 - 종가가 음수이면 저장되지 않는다")
	@Test
	void savePrice_whenNegativePrice_thenDoNotStoreClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long closingPrice = -100L;

		// when
		closingPriceService.savePrice(tickerSymbol, closingPrice);

		// then
		Assertions.assertThat(closingPriceRepository.fetchPrice(tickerSymbol)).isEmpty();
	}

	@DisplayName("종목 종가 저장 - 종가가 0이어도 저장된다")
	@Test
	void savePrice_whenZeroPrice_thenStoreClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long closingPrice = 0L;

		// when
		closingPriceService.savePrice(tickerSymbol, closingPrice);

		// then
		ClosingPriceRedisEntity entity = closingPriceRepository.fetchPrice(tickerSymbol).orElseThrow();
		Assertions.assertThat(entity)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", closingPrice);
	}

	@DisplayName("종목 종가 저장 - 티커가 유효하지 않으면 저장되지 않는다")
	@Test
	void savePrice_whenInvalidTicker_thenDoNotStoreClosingPrice() {
		// given
		long closingPrice = 60000L;

		// when
		closingPriceService.savePrice("", closingPrice);
		closingPriceService.savePrice(" ", closingPrice);
		closingPriceService.savePrice(null, closingPrice);

		// then
		Assertions.assertThat(closingPriceRepository.fetchPrice("")).isEmpty();
		Assertions.assertThat(closingPriceRepository.fetchPrice(" ")).isEmpty();
		Assertions.assertThat(closingPriceRepository.fetchPrice(null)).isEmpty();
	}
}
