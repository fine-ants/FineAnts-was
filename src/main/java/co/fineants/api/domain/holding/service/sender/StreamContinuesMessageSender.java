package co.fineants.api.domain.holding.service.sender;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.factory.PortfolioSseEventBuilderFactory;
import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
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
		SseEventBuilderFactory factory = new PortfolioSseEventBuilderFactory(reconnectTimeMillis);
		SseEmitter.SseEventBuilder builder = factory.create(message);
		try {
			emitter.send(builder);
		} catch (Exception exception) {
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		}
	}
}
