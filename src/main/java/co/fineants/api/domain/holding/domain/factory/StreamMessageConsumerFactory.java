package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;

public interface StreamMessageConsumerFactory {
	StreamSseMessageSender createConsumer(PortfolioStreamer streamer, SseEmitter emitter);
}
