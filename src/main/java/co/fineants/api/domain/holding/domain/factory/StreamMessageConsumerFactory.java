package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.sender.StreamMessageSender;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;

public interface StreamMessageConsumerFactory {
	StreamMessageSender createConsumer(PortfolioStreamer streamer, SseEmitter emitter);
}
