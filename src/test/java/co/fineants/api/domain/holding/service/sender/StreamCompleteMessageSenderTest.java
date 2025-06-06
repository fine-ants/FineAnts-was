package co.fineants.api.domain.holding.service.sender;

import static org.mockito.BDDMockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
import co.fineants.api.domain.holding.domain.message.PortfolioCompleteStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;

class StreamCompleteMessageSenderTest {

	private StreamSseMessageSender sender;
	private SseEmitter emitter;
	private SseEventBuilderFactory sseEventBuilderFactory;

	@BeforeEach
	void setUp() {
		emitter = Mockito.mock(SseEmitter.class);
		sseEventBuilderFactory = Mockito.mock(SseEventBuilderFactory.class);
		sender = new StreamCompleteMessageSender(emitter, sseEventBuilderFactory);
	}

	@DisplayName("StreamMessage를 SseEmitter로 전송한다.")
	@Test
	void givenStreamMessage_whenAcceptMessage_thenSendEmitter() throws IOException {
		// given
		StreamMessage message = new PortfolioCompleteStreamMessage();
		SseEmitter.SseEventBuilder builder = Mockito.mock(SseEmitter.SseEventBuilder.class);
		given(sseEventBuilderFactory.create(message))
			.willReturn(builder);
		// when
		sender.accept(message);
		// then
		verify(emitter).send(builder);
		verify(emitter).complete();
		verify(emitter, never()).completeWithError(any());
	}
}
