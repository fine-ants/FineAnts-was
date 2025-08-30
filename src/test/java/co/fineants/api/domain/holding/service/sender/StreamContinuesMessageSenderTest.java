package co.fineants.api.domain.holding.service.sender;

import static org.mockito.BDDMockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class StreamContinuesMessageSenderTest {

	private StreamSseMessageSender sender;
	private SseEmitter emitter;

	@BeforeEach
	void setUp() {
		emitter = Mockito.mock(SseEmitter.class);
		sender = new StreamContinuesMessageSender(emitter);
	}

	@DisplayName("StreamMessage가 주어지고 메시지를 접수하면 SseEmitter에 SseEventBuilder를 전송한다.")
	@Test
	void givenStreamMessage_whenAcceptMessage_thenSendSseEventBuilder() throws IOException {
		// given
		SseEmitter.SseEventBuilder builder = Mockito.mock(SseEmitter.SseEventBuilder.class);
		// when
		sender.accept(builder);
		// then
		BDDMockito.verify(emitter).send(ArgumentMatchers.any(SseEmitter.SseEventBuilder.class));
		BDDMockito.verify(emitter, never()).complete();
		BDDMockito.verify(emitter, never()).completeWithError(any());
	}

	@DisplayName("StreamMessage가 주어지고 IO 예외가 발생하면 SseEmitter를 에러로 완료한다.")
	@Test
	void givenStreamMessage_whenRaisedInputOutputError_thenCompleteWithError() throws IOException {
		// given
		SseEmitter.SseEventBuilder builder = Mockito.mock(SseEmitter.SseEventBuilder.class);
		IOException exception = new IOException("Test exception");
		willThrow(exception)
			.given(emitter).send(ArgumentMatchers.any(SseEmitter.SseEventBuilder.class));
		// when
		sender.accept(builder);
		// then
		BDDMockito.verify(emitter).completeWithError(exception);
	}
}
