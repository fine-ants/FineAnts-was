package co.fineants.api.domain.holding.service.sender;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamCompleteMessageSender implements StreamSseMessageSender {

	private final SseEmitter emitter;

	public StreamCompleteMessageSender(SseEmitter emitter) {
		this.emitter = emitter;
	}

	@Override
	public void accept(SseEmitter.SseEventBuilder builder) {
		try {
			emitter.send(builder);
		} catch (IOException exception) {
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		} finally {
			emitter.complete();
		}
	}
}
