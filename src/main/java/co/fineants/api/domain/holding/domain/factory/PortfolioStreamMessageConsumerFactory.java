package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.sender.StreamCompleteMessageSender;
import co.fineants.api.domain.holding.service.sender.StreamContinuesMessageSender;
import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;

public class PortfolioStreamMessageConsumerFactory implements StreamMessageConsumerFactory {

	private final long reconnectTimeMillis;

	public PortfolioStreamMessageConsumerFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public StreamSseMessageSender createStreamContinuesMessageSender(SseEmitter emitter) {
		return new StreamContinuesMessageSender(emitter, reconnectTimeMillis);
	}

	@Override
	public StreamSseMessageSender createStreamCompleteMessageSender(SseEmitter emitter) {
		SseEventBuilderFactory sseEventBuilderFactory = new PortfolioSseEventBuilderFactory(reconnectTimeMillis);
		return new StreamCompleteMessageSender(emitter, sseEventBuilderFactory);
	}
}
