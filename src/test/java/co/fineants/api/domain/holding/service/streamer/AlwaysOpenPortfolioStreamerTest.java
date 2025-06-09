package co.fineants.api.domain.holding.service.streamer;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import reactor.test.StepVerifier;

class AlwaysOpenPortfolioStreamerTest {

	private PortfolioHoldingService portfolioHoldingService;
	private Long portfolioId;
	private Duration interval;

	@BeforeEach
	void setUp() {
		portfolioHoldingService = Mockito.mock(PortfolioHoldingService.class);
		portfolioId = 1L;
		StreamMessage message = Mockito.mock(StreamMessage.class);
		BDDMockito.given(portfolioHoldingService.getPortfolioReturns(portfolioId))
			.willReturn(message);
		interval = Duration.ofSeconds(5);
	}

	@DisplayName("반드시 열려있는 포트폴리오 스트리머는 주어진 간격과 최대 개수에 따라 메시지를 스트리밍한다.")
	@Test
	void streamMessages_ShouldReturnStreamOfMessages() {
		// given
		long maxCount = 6L;
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, interval, maxCount);
		// when & then
		StepVerifier.withVirtualTime(() -> streamer.streamMessages(portfolioId))
			.thenAwait(interval.multipliedBy(maxCount))
			.expectNextCount(maxCount)
			.verifyComplete();
	}

	@DisplayName("개수가 0개인 경우 빈 Flux를 반환한다")
	@Test
	void givenPortfolioStreamer_whenMaxCountIsZero_thenReturnEmptyFlux() {
		// given
		long maxCount = 0L;
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, interval, maxCount);
		// when & then
		StepVerifier.withVirtualTime(() -> streamer.streamMessages(portfolioId))
			.expectNextCount(0L)
			.verifyComplete();
	}

	@DisplayName("maxCount의 값이 음수인 경우 예외를 발생시킨다")
	@Test
	void givenNegativeMaxCount_whenCreateInstance_thenThrowException() {
		// given
		long maxCount = -1;
		// when
		Throwable throwable = catchThrowable(
			() -> new AlwaysOpenPortfolioStreamer(portfolioHoldingService, interval, maxCount));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class);
	}
}
