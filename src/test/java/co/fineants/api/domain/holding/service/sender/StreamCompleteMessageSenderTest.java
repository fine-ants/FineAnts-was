package co.fineants.api.domain.holding.service.sender;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.PortfolioCompleteStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;

class StreamCompleteMessageSenderTest {

	private StreamSseMessageSender sender;
	private SseEmitter emitter;

	@BeforeEach
	void setUp() {
		emitter = Mockito.mock(SseEmitter.class);
		long reconnectTimeMillis = 3000L;
		sender = new StreamCompleteMessageSender(emitter, reconnectTimeMillis);
	}

	@DisplayName("StreamMessage를 SseEmitter로 전송한다.")
	@Test
	void givenStreamMessage_whenAcceptMessage_thenSendEmitter() throws IOException {
		// given
		StreamMessage message = new PortfolioCompleteStreamMessage();
		// when
		sender.accept(message);
		// then
		// assert emitter send
		BDDMockito.verify(emitter).send(ArgumentMatchers.any(SseEmitter.SseEventBuilder.class));
	}
}
