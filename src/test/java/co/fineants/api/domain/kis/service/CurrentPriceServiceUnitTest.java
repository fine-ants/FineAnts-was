package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusChecker;
import co.fineants.api.domain.holiday.domain.entity.Holiday;
import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.event.StockCurrentPriceRefreshEvent;
import co.fineants.stock.event.StockCurrentPriceRequiredEvent;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class CurrentPriceServiceUnitTest {

	private CurrentPriceService service;

	@Mock
	private CurrentPriceRepository currentPriceRepository;

	@Mock
	private KisService kisService;

	@Mock
	private Clock clock;

	private long freshnessThresholdMillis;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Mock
	private HolidayService holidayService;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@Mock
	private MarketStatusChecker marketStatusChecker;

	@BeforeEach
	void setUp() {
		freshnessThresholdMillis = 5000L;
		// BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
		// 	.willReturn(LocalDateTime.of(2026, 2, 12, 9, 0)); // 목요일

		service = new CurrentPriceService(currentPriceRepository, clock, freshnessThresholdMillis, eventPublisher,
			marketStatusChecker, localDateTimeService);
	}

	@DisplayName("종목 현재가 저장 - 정상 저장된다")
	@Test
	void should_save_current_price() {
		// given
		String tickerSymbol = "005930";
		long priceToSave = 60000L;

		// when
		service.savePrice(tickerSymbol, priceToSave);

		// then
		BDDMockito.verify(currentPriceRepository, Mockito.times(1))
			.savePrice(tickerSymbol, priceToSave);
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 현재가가 없어서 동기적 이벤트를 발행하고, 외부 API에서 조회한 현재가를 반환한다.")
	@Test
	void should_publish_current_price_required_event_when_current_price_is_cache_miss() {
		// given
		String tickerSymbol = "005930";
		long freshPrice = 50000L;
		CurrentPriceRedisEntity entity = CurrentPriceRedisEntity.of(tickerSymbol, freshPrice, 1_000_000);
		BDDMockito.given(currentPriceRepository.fetchPriceBy(tickerSymbol))
			.willReturn(Optional.empty())
			.willReturn(Optional.of(entity));
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);
		// then
		BDDMockito.verify(eventPublisher, Mockito.times(1))
			.publishEvent(new StockCurrentPriceRequiredEvent(tickerSymbol));
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(freshPrice));
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 신선한 현재가가 있어서 바로 반환한다.")
	@Test
	void should_return_fresh_current_price_when_price_is_fresh() {
		// given
		String tickerSymbol = "005930";
		long expectedPrice = 50000L;
		CurrentPriceRedisEntity entity = CurrentPriceRedisEntity.of(tickerSymbol, expectedPrice, 1_000_000);
		BDDMockito.given(currentPriceRepository.fetchPriceBy(tickerSymbol))
			.willReturn(Optional.of(entity));

		// when
		Money price = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(price).isEqualTo(Money.won(50000L));
	}

	@DisplayName("종목 현재가 조회 - 캐시 저장소에 종목 현재가가 신선도(freshness) 기준에 맞지 않아서 비동기적 이벤트를 발행하고, 기존 현재가를 반환해야 한다.")
	@Test
	void should_publish_current_price_async_event_when_current_price_is_stale_then_return_stale_current_price() {
		// given
		String tickerSymbol = "005930";
		long stalePrice = 45000L;

		CurrentPriceRedisEntity staleEntity = CurrentPriceRedisEntity.of(tickerSymbol, stalePrice, 1_000_000);
		BDDMockito.given(currentPriceRepository.fetchPriceBy(tickerSymbol))
			.willReturn(Optional.of(staleEntity));
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		// 마켓 체커 모킹하기
		LocalDateTime time = LocalDate.of(2026, 7, 24).atTime(9, 0);
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(time);
		BDDMockito.given(marketStatusChecker.isOpen(time))
			.willReturn(true);
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		BDDMockito.verify(eventPublisher, Mockito.times(1))
			.publishEvent(new StockCurrentPriceRefreshEvent(tickerSymbol));
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
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

	@DisplayName("종목 현재가 조회 - 장시간 외에서 신선한 데이터가 존재하면 해당 데이터를 반환한다")
	@Test
	void fetchPrice_whenMarketIsCloseAndCurrentPriceIsFresh_thenReturnCurrentPrice() {
		// given
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L);  // initial time

		String tickerSymbol = "005930";
		long expectedPrice = 50000L;
		currentPriceRepository.savePrice(tickerSymbol, expectedPrice);

		// when
		Money price = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(price).isEqualTo(Money.won(50000L));
	}

	@DisplayName("종목 현재가 조회 - 장시간 외에서 신선하지 않은 데이터가 존재하면 비동기 이벤트를 갱신하지 않고 기존 데이터를 반환한다")
	@ParameterizedTest
	@MethodSource(value = {"co.fineants.TestDataProvider#provideMarketCloseTime"})
	void fetchPrice_whenMarketIsCloseAndCurrentPriceIsStale_thenReturnCurrentPriceWithoutRefresh(LocalDateTime now,
		String ignoredDescription) {
		// given
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(now);

		String tickerSymbol = "005930";
		long stalePrice = 45000L;

		currentPriceRepository.savePrice(tickerSymbol, stalePrice);
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
		Assertions.assertThat(currentPriceRepository.fetchPriceBy(tickerSymbol).orElseThrow())
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", stalePrice);
		// 이벤트는 비즈니스 흐름상 발행될 수 있으나, 리스너의 필터링 로직에 의해 고비용 작업인 API 호출이 차단됨을 검증함
		BDDMockito.verify(kisService, BDDMockito.never()).fetchCurrentPrice(tickerSymbol);
	}

	@DisplayName("종목 현재가 조회 - 공휴일에는 비동기 갱신하지 않고 기존 데이터를 반환한다")
	@Test
	void fetchPrice_whenTodayIsHoliday_thenReturnCurrentPriceWithoutRefresh() {
		// given
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		LocalDate now = LocalDate.of(2026, 2, 16); // 월요일 휴장
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(now.atTime(9, 0));
		Holiday holiday = Holiday.close(now);
		holidayService.saveHoliday(holiday);

		String tickerSymbol = "005930";
		long stalePrice = 45000L;

		currentPriceRepository.savePrice(tickerSymbol, stalePrice);
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
		Assertions.assertThat(currentPriceRepository.fetchPriceBy(tickerSymbol).orElseThrow())
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", stalePrice);
		// 이벤트는 비즈니스 흐름상 발행될 수 있으나, 리스너의 필터링 로직에 의해 고비용 작업인 API 호출이 차단됨을 검증함
		BDDMockito.verify(kisService, BDDMockito.never()).fetchCurrentPrice(tickerSymbol);
	}

	@DisplayName("모든 종목 티커 조회 - 저장된 모든 종목 티커에 대한 현재가를 조회한다")
	@Test
	void getAllTickers_thenReturnAllTickers() {
		// given
		currentPriceRepository.savePrice("005930", 50000L);
		currentPriceRepository.savePrice("000660", 30000L);

		// when
		Set<String> actual = service.getAllTickers();

		// then
		Assertions.assertThat(actual)
			.containsExactlyInAnyOrder("005930", "000660");
	}
}
