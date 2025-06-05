package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.StreamMessageConsumer;

public interface StreamMessageConsumerFactory {
	StreamMessageConsumer createConsumer(SseEmitter emitter);
}
