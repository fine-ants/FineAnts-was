package co.fineants.api.domain.holding.service;

import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import co.fineants.api.global.common.time.LocalDateTimeService;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class FluxIntervalPortfolioStreamerTest {

	private PortfolioStreamer service;
	private StockMarketChecker stockMarketChecker;
	private LocalDateTimeService localDateTimeService;

	@BeforeEach
	void setUp() {
		PortfolioHoldingService holdingService = Mockito.mock(PortfolioHoldingService.class);
		LocalDateTime dateTime = LocalDateTime.of(2025, 6, 4, 9, 0);
		stockMarketChecker = new StockMarketChecker();
		localDateTimeService = Mockito.mock(LocalDateTimeService.class);
		given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(dateTime); // 기본적으로 장 시간을 반환하도록 설정
		int second = 5;
		int maxCount = 6;
		service = new FluxIntervalPortfolioStreamer(holdingService, stockMarketChecker, localDateTimeService, second,
			maxCount);

		PortfolioHoldingsRealTimeResponse response = Mockito.mock(PortfolioHoldingsRealTimeResponse.class);
		Mockito.when(holdingService.readMyPortfolioStocksInRealTime(Mockito.anyLong()))
			.thenReturn(response);
	}

	@DisplayName("포트폴리오의 수익률 데이터를 스트리밍한다.")
	@Test
	void streamPortfolioReturns() {
		// given
		long portfolioId = 1L;
		// when & then
		StepVerifier.withVirtualTime(() -> service.streamReturns(portfolioId))
			.thenAwait(Duration.ofSeconds(30))
			.expectNextCount(6)
			.verifyComplete();
	}

	@DisplayName("장시간이 끝난후에는 빈 스트림을 반환한다.")
	@Test
	void givenPortfolioId_whenMarketIsClosed_thenReturnEmptyStream() {
		// given
		long portfolioId = 1L;
		LocalDateTime closedTime = LocalDateTime.of(2025, 6, 4, 15, 30);
		given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(closedTime);
		// when
		Flux<PortfolioHoldingsRealTimeResponse> flux = service.streamReturns(portfolioId);
		// then
		StepVerifier.create(flux)
			.expectSubscription()
			.expectNextCount(0)
			.verifyComplete();
	}
}
