package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.StreamMessage;

public class PortfolioSseEventBuilderFactory implements SseEventBuilderFactory {
	private final long reconnectTimeMillis;
	private final UuidGenerator uuidGenerator;

	public PortfolioSseEventBuilderFactory(long reconnectTimeMillis, UuidGenerator uuidGenerator) {
		this.reconnectTimeMillis = reconnectTimeMillis;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public SseEmitter.SseEventBuilder create(StreamMessage message) {
		String id = uuidGenerator.generate();
		return SseEmitter.event()
			.id(id)
			.data(message.getData())
			.name(message.getEventName())
			.reconnectTime(reconnectTimeMillis);
	}
}
