package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.StreamMessageSender;

public interface StreamMessageConsumerFactory {
	StreamMessageSender createConsumer(SseEmitter emitter);
}
