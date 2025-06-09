package co.fineants.api.domain.holding.service.sender;

import static org.mockito.BDDMockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class StreamCompleteMessageSenderTest {

	private StreamSseMessageSender sender;
	private SseEmitter emitter;

	@BeforeEach
	void setUp() {
		emitter = Mockito.mock(SseEmitter.class);
		sender = new StreamCompleteMessageSender(emitter);
	}

	@DisplayName("StreamMessage를 SseEmitter로 전송한다.")
	@Test
	void givenStreamMessage_whenAcceptMessage_thenSendEmitter() throws IOException {
		// given
		SseEmitter.SseEventBuilder builder = Mockito.mock(SseEmitter.SseEventBuilder.class);
		// when
		sender.accept(builder);
		// then
		verify(emitter).send(builder);
		verify(emitter).complete();
		verify(emitter, never()).completeWithError(any());
	}
}
