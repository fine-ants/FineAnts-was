package co.fineants.api.domain.holding.domain.factory;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.StreamMessage;

public class PortfolioSseEventBuilderFactory implements SseEventBuilderFactory {
	private final long reconnectTimeMillis;

	public PortfolioSseEventBuilderFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public SseEmitter.SseEventBuilder create(StreamMessage message) {
		String id = UUID.randomUUID().toString();
		return SseEmitter.event()
			.id(id)
			.data(message.getData())
			.name(message.getEventName())
			.reconnectTime(reconnectTimeMillis);
	}
}
