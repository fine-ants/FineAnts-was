package co.fineants.api.domain.holding.service;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.PortfolioStreamMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortfolioStreamMessageSseSender implements PortfolioStreamMessageConsumer {

	private final SseEmitter emitter;
	private final long reconnectTimeMillis;

	public PortfolioStreamMessageSseSender(SseEmitter emitter, long reconnectTimeMillis) {
		this.emitter = emitter;
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public void accept(PortfolioStreamMessage message) {
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
