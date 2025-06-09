package co.fineants.api.domain.holding.service.sender;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamCompleteMessageSender implements StreamSseMessageSender {

	private final SseEmitter emitter;
	private final SseEventBuilderFactory sseEventBuilderFactory;

	public StreamCompleteMessageSender(SseEmitter emitter,
		SseEventBuilderFactory sseEventBuilderFactory) {
		this.emitter = emitter;
		this.sseEventBuilderFactory = sseEventBuilderFactory;
	}

	@Override
	public void accept(StreamMessage message) {
		SseEmitter.SseEventBuilder builder = sseEventBuilderFactory.create(message);
		try {
			emitter.send(builder);
		} catch (IOException exception) {
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		}
		emitter.complete();
	}
}
