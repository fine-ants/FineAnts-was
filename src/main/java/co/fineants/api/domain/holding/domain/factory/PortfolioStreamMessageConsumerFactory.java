package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.sender.StreamCompleteMessageSender;
import co.fineants.api.domain.holding.service.sender.StreamMessageSender;
import co.fineants.api.domain.holding.service.sender.StreamMessageSseSender;
import co.fineants.api.domain.holding.service.streamer.FluxIntervalPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;

public class PortfolioStreamMessageConsumerFactory implements StreamMessageConsumerFactory {

	private final long reconnectTimeMillis;

	public PortfolioStreamMessageConsumerFactory(long reconnectTimeMillis) {
		this.reconnectTimeMillis = reconnectTimeMillis;
	}

	@Override
	public StreamMessageSender createConsumer(PortfolioStreamer streamer, SseEmitter emitter) {
		if (streamer instanceof FluxIntervalPortfolioStreamer) {
			return new StreamMessageSseSender(emitter, reconnectTimeMillis);
		}
		return new StreamCompleteMessageSender(emitter, reconnectTimeMillis);
	}
}
