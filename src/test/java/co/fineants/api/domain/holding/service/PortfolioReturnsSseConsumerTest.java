package co.fineants.api.domain.holding.service;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;

class PortfolioReturnsSseConsumerTest {

	private SseEmitter emitter;
	private Consumer<PortfolioHoldingsRealTimeResponse> consumer;

	@BeforeEach
	void setUp() {
		emitter = mock(SseEmitter.class);
		consumer = new PortfolioReturnsSseConsumer(emitter);
	}

	@DisplayName("data가 전달되면 SseEmitter에 이벤트를 전송한다.")
	@Test
	void accept_ShouldSendEvent_WhenDataIsReceived() throws IOException {
		// given
		PortfolioHoldingsRealTimeResponse data = mock(PortfolioHoldingsRealTimeResponse.class);
		// when
		consumer.accept(data);
		// then
		verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
		verify(emitter, never()).completeWithError(any());
	}

	@DisplayName("IOException이 발생하면 SseEmitter를 에러로 완료한다.")
	@Test
	void accept_ShouldCompleteWithError_WhenIOExceptionOccurs() throws IOException {
		// given
		PortfolioHoldingsRealTimeResponse data = mock(PortfolioHoldingsRealTimeResponse.class);
		willThrow(new IOException("Test Exception"))
			.given(emitter).send(any(SseEmitter.SseEventBuilder.class));
		// when
		consumer.accept(data);
		// then
		verify(emitter).completeWithError(any(IOException.class));
	}
}
