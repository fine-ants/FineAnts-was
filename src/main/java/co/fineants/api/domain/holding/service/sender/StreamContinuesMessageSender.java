package co.fineants.api.domain.holding.service.sender;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
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
	public void accept(StreamMessage message) {
		// todo: 구현체별 중복 제거
		String id = UUID.randomUUID().toString();
		SseEmitter.SseEventBuilder builder = SseEmitter.event()
			.id(id)
			.data(message.getData())
			.name(message.getEventName())
			.reconnectTime(reconnectTimeMillis);
		try {
			emitter.send(builder);
		} catch (Exception exception) {
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		}
	}
}
