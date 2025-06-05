package co.fineants.api.domain.holding.service;

import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
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

		StreamMessage portfolioStreamMessage = mock(StreamMessage.class);
		when(holdingService.getPortfolioReturns(anyLong()))
			.thenReturn(portfolioStreamMessage);
	}

	@DisplayName("포트폴리오의 수익률 메시지를 스트리밍한다.")
	@Test
	void streamPortfolioMessages() {
		// given
		long portfolioId = 1L;
		long totalTime = intervalSecond * maxCount;
		// when & then
		StepVerifier.withVirtualTime(() -> service.streamMessages(portfolioId))
			.thenAwait(Duration.ofSeconds(totalTime))
			.expectNextCount(maxCount)
			.verifyComplete();
	}
}
