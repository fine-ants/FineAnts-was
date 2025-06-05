package co.fineants.api.domain.holding.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.domain.message.PortfolioCompleteStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.streamer.ClosedMarketPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class ClosedMarketPortfolioStreamerTest {

	private PortfolioStreamer portfolioStreamer;

	@BeforeEach
	void setUp() {
		MarketStatusChecker stockMarketChecker = Mockito.mock(WeekdayMarketStatusChecker.class);
		portfolioStreamer = new ClosedMarketPortfolioStreamer(stockMarketChecker);
	}

	@DisplayName("장시간이 아닌 경우에는 완료 메시지를 스트리밍한다")
	@Test
	void whenMarketIsClose_thenReturnCompleteMessage() {
		// given
		Long portfolioId = 1L;
		// when
		Flux<StreamMessage> flux = portfolioStreamer.streamMessages(portfolioId);
		// then
		PortfolioCompleteStreamMessage expected = new PortfolioCompleteStreamMessage();
		StepVerifier.create(flux)
			.expectNext(expected)
			.verifyComplete();
	}
}
