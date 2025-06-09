package co.fineants.api.domain.holding.service.sender;

import java.io.IOException;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamContinuesMessageSender implements StreamSseMessageSender {

	private final SseEmitter emitter;
	private final long reconnectTimeMillis;

	public StreamContinuesMessageSender(SseEmitter emitter, long reconnectTimeMillis) {
		this.emitter = emitter;
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public void accept(SseEmitter.SseEventBuilder builder) {
		try {
			emitter.send(builder);
		} catch (IOException exception) {
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		}
	}
}
