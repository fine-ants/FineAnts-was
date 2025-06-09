package co.fineants.api.domain.holding.service.streamer;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import reactor.test.StepVerifier;

class AlwaysOpenPortfolioStreamerTest {

	@DisplayName("반드시 열려있는 포트폴리오 스트리머는 주어진 간격과 최대 개수에 따라 메시지를 스트리밍한다.")
	@Test
	void streamMessages_ShouldReturnStreamOfMessages() {
		// given
		PortfolioHoldingService portfolioHoldingService = Mockito.mock(PortfolioHoldingService.class);
		Long portfolioId = 1L;
		StreamMessage message = Mockito.mock(StreamMessage.class);
		BDDMockito.given(portfolioHoldingService.getPortfolioReturns(portfolioId))
			.willReturn(message);
		Duration interval = Duration.ofSeconds(5);
		long maxCount = 6L;
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, interval, maxCount);
		// when & then
		StepVerifier.withVirtualTime(() -> streamer.streamMessages(portfolioId))
			.thenAwait(interval.multipliedBy(maxCount))
			.expectNextCount(maxCount)
			.verifyComplete();
	}
}
