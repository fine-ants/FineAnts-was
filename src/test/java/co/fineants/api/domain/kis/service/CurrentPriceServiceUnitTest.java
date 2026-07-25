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
import co.fineants.api.domain.holiday.service.HolidayService;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.event.StockCurrentPriceRefreshEvent;
import co.fineants.stock.event.StockCurrentPriceRequiredEvent;

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

	@DisplayName("종목 현재가 조회 - 장시간 외에서 신선한 데이터가 존재하면 해당 데이터를 반환한다")
	@Test
	void should_return_fresh_price_when_current_price_is_fresh_and_market_is_close() {
		// given
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L);  // initial time

		String tickerSymbol = "005930";
		long expectedPrice = 50000L;

		CurrentPriceRedisEntity freshEntity = CurrentPriceRedisEntity.of(tickerSymbol, expectedPrice, 1_000_000);
		BDDMockito.given(currentPriceRepository.fetchPriceBy(tickerSymbol))
			.willReturn(Optional.of(freshEntity));
		// when
		Money price = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(price).isEqualTo(Money.won(50000L));
	}

	// todo: convert to unit test
	@DisplayName("종목 현재가 조회 - 장시간 외에서 신선하지 않은 데이터가 존재하면 비동기 이벤트를 갱신하지 않고 기존 데이터를 반환한다")
	@ParameterizedTest
	@MethodSource(value = {"co.fineants.TestDataProvider#provideMarketCloseTime"})
	void should_return_stale_price_when_current_price_is_stale_and_market_is_closed(LocalDateTime now,
		String ignoredDescription) {
		// given

		String tickerSymbol = "005930";
		long stalePrice = 45000L;

		currentPriceRepository.savePrice(tickerSymbol, stalePrice);
		CurrentPriceRedisEntity staleEntity = CurrentPriceRedisEntity.of(tickerSymbol, stalePrice, 1_000_000);
		BDDMockito.given(currentPriceRepository.fetchPriceBy(tickerSymbol))
			.willReturn(Optional.of(staleEntity));
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(now);
		BDDMockito.given(marketStatusChecker.isOpen(now))
			.willReturn(false);
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
	}

	@DisplayName("종목 현재가 조회 - 공휴일에는 비동기 갱신하지 않고 기존 데이터를 반환한다")
	@Test
	void should_return_existing_current_price_when_today_is_holiday() {
		// given
		BDDMockito.given(clock.millis())
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		LocalDateTime now = LocalDate.of(2026, 2, 16).atTime(9, 0); // 월요일 휴장
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(now);

		String tickerSymbol = "005930";
		long stalePrice = 45000L;

		currentPriceRepository.savePrice(tickerSymbol, stalePrice);
		CurrentPriceRedisEntity staleEntity = CurrentPriceRedisEntity.of(tickerSymbol, stalePrice, 1_000_000);
		BDDMockito.given(currentPriceRepository.fetchPriceBy(tickerSymbol))
			.willReturn(Optional.of(staleEntity));
		BDDMockito.given(marketStatusChecker.isOpen(now))
			.willReturn(false);

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
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
