package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.StreamMessageSender;
import co.fineants.api.domain.holding.service.StreamMessageSseSender;

public class PortfolioStreamMessageConsumerFactory implements StreamMessageConsumerFactory {

	private final long reconnectTimeMillis;

	public PortfolioStreamMessageConsumerFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	public StreamMessageSender createConsumer(SseEmitter emitter) {
		return new StreamMessageSseSender(emitter, reconnectTimeMillis);
	}
}
