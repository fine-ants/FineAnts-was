package co.fineants.api.domain.holding.service;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import reactor.test.StepVerifier;

class FluxIntervalPortfolioStreamerTest {

	private PortfolioStreamer service;

	@BeforeEach
	void setUp() {
		PortfolioHoldingService holdingService = Mockito.mock(PortfolioHoldingService.class);
		int second = 5;
		int maxCount = 6;
		service = new FluxIntervalPortfolioStreamer(holdingService, second, maxCount);

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
}
