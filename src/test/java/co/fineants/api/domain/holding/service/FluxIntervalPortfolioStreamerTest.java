package co.fineants.api.domain.holding.service;

import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import reactor.test.StepVerifier;

class FluxIntervalPortfolioStreamerTest {

	private PortfolioStreamer service;
	private long intervalSecond;
	private long maxCount;

	@BeforeEach
	void setUp() {
		PortfolioHoldingService holdingService = mock(PortfolioHoldingService.class);
		StockMarketChecker stockMarketChecker = mock(StockMarketChecker.class);
		intervalSecond = 5;
		maxCount = 6;
		service = new FluxIntervalPortfolioStreamer(holdingService, stockMarketChecker, intervalSecond, maxCount);

		PortfolioHoldingsRealTimeResponse response = mock(PortfolioHoldingsRealTimeResponse.class);
		when(holdingService.readMyPortfolioStocksInRealTime(anyLong()))
			.thenReturn(response);
	}

	@DisplayName("포트폴리오의 수익률 데이터를 스트리밍한다.")
	@Test
	void streamPortfolioReturns() {
		// given
		long portfolioId = 1L;
		long totalTime = intervalSecond * maxCount;
		// when & then
		StepVerifier.withVirtualTime(() -> service.streamReturns(portfolioId))
			.thenAwait(Duration.ofSeconds(totalTime))
			.expectNextCount(maxCount)
			.verifyComplete();
	}
}
