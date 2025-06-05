package co.fineants.api.domain.holding.service;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.PortfolioStreamMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortfolioStreamMessageSseSender implements PortfolioStreamMessageConsumer {

	private final SseEmitter emitter;

	public PortfolioStreamMessageSseSender(SseEmitter emitter) {
		this.emitter = emitter;
	}

	@Override
	public void accept(PortfolioStreamMessage message) {
		String id = UUID.randomUUID().toString();
		SseEmitter.SseEventBuilder builder = SseEmitter.event()
			.id(id)
			.data(message.getData())
			.name(message.getEventName())
			.reconnectTime(3000L);
		try {
			emitter.send(builder);
		} catch (Exception exception) {
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		}
	}
}
