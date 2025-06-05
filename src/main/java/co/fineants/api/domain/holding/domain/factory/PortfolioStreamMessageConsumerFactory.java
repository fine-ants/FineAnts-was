package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.PortfolioStreamMessageSseSender;
import co.fineants.api.domain.holding.service.StreamMessageConsumer;

public class PortfolioStreamMessageConsumerFactory implements StreamMessageConsumerFactory {

	private final long reconnectTimeMillis;

	public PortfolioStreamMessageConsumerFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	public StreamMessageConsumer createConsumer(SseEmitter emitter) {
		return new PortfolioStreamMessageSseSender(emitter, reconnectTimeMillis);
	}
}
