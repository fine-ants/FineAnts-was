package co.fineants.api.domain.holding.service.streamer;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.factory.StreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.sender.StreamContinuesMessageSender;
import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;
import reactor.test.StepVerifier;

class AlwaysOpenPortfolioStreamerTest {

	private PortfolioHoldingService portfolioHoldingService;
	private Long portfolioId;
	private long second;
	private long maxCount;

	@BeforeEach
	void setUp() {
		portfolioHoldingService = Mockito.mock(PortfolioHoldingService.class);
		portfolioId = 1L;
		StreamMessage message = Mockito.mock(StreamMessage.class);
		BDDMockito.given(portfolioHoldingService.getPortfolioReturns(portfolioId))
			.willReturn(message);
		second = 5;
		maxCount = 6L;
	}

	@DisplayName("반드시 열려있는 포트폴리오 스트리머는 주어진 간격과 최대 개수에 따라 메시지를 스트리밍한다.")
	@Test
	void streamMessages_ShouldReturnStreamOfMessages() {
		// given
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, second, maxCount);
		// when & then
		StepVerifier.withVirtualTime(() -> streamer.streamMessages(portfolioId))
			.thenAwait(Duration.ofSeconds(second * maxCount))
			.expectNextCount(maxCount)
			.verifyComplete();
	}

	@DisplayName("개수가 0개인 경우 빈 Flux를 반환한다")
	@Test
	void givenPortfolioStreamer_whenMaxCountIsZero_thenReturnEmptyFlux() {
		// given
		maxCount = 0L;
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, second, maxCount);
		// when & then
		StepVerifier.withVirtualTime(() -> streamer.streamMessages(portfolioId))
			.expectNextCount(0L)
			.verifyComplete();
	}

	@DisplayName("maxCount의 값이 음수인 경우 예외를 발생시킨다")
	@Test
	void givenNegativeMaxCount_whenCreateInstance_thenThrowException() {
		// given
		maxCount = -1;
		// when
		Throwable throwable = catchThrowable(
			() -> new AlwaysOpenPortfolioStreamer(portfolioHoldingService, second, maxCount));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("구현체 타입이 AlwaysOpenPortfolioStreamer인 경우 일자, 시간, 공휴일 상관없이 언제나 true를 반환한다")
	@Test
	void givenPortfolioStreamer_whenAlwaysOpenPortfolioStreamer_thenReturnTrue() {
		// given
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, second, maxCount);
		// when
		boolean supports = streamer.supports(LocalDateTime.now());
		// then
		Assertions.assertThat(supports).isTrue();
	}

	@DisplayName("AlwaysOpenPortfolioStreamer 객체가 StreamSseMessageSender 객체를 생성시 StreamContinuesMessageSender를 반환한다")
	@Test
	void givenPortfolioStreamer_whenAlwaysOpenPortfolioStreamer_thenReturnStreamContinuesMessageSender() {
		// given
		PortfolioStreamer streamer = new AlwaysOpenPortfolioStreamer(portfolioHoldingService, second, maxCount);
		SseEmitter emitter = Mockito.mock(SseEmitter.class);
		StreamMessageConsumerFactory factory = Mockito.mock(StreamMessageConsumerFactory.class);
		BDDMockito.given(factory.createStreamContinuesMessageSender(emitter))
			.willReturn(Mockito.mock(StreamContinuesMessageSender.class));
		// when
		StreamSseMessageSender sender = streamer.createStreamSseMessageSender(emitter, factory);
		// then
		Assertions.assertThat(sender)
			.isInstanceOf(StreamContinuesMessageSender.class);
	}
}
