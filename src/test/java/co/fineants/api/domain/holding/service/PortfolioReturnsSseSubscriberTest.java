package co.fineants.api.domain.holding.service;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;

class PortfolioReturnsSseSubscriberTest {

	private SseEmitter emitter;
	private PortfolioReturnsSubscriber subscriber;

	@BeforeEach
	void setUp() {
		emitter = mock(SseEmitter.class);
		subscriber = new PortfolioReturnsSseSubscriber(emitter);
	}

	@DisplayName("data가 전달되면 SseEmitter에 이벤트를 전송한다.")
	@Test
	void accept_ShouldSendEvent_WhenDataIsReceived() throws IOException {
		// given
		PortfolioHoldingsRealTimeResponse data = mock(PortfolioHoldingsRealTimeResponse.class);
		// when
		subscriber.accept(data);
		// then
		verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
		verify(emitter, never()).completeWithError(any());
	}
}
