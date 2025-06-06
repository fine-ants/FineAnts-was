package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.message.StreamMessage;

public interface SseEventBuilderFactory {
	SseEmitter.SseEventBuilder create(StreamMessage message);
}
