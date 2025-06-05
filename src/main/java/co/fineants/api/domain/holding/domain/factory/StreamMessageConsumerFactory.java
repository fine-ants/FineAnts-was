package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.sender.StreamSseMessageSender;

public interface StreamMessageConsumerFactory {
	StreamSseMessageSender createStreamContinuesMessageSender(SseEmitter emitter);

	StreamSseMessageSender createStreamCompleteMessageSender(SseEmitter emitter);
}
