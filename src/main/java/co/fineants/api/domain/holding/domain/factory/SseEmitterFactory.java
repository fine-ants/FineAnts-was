package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterFactory {
	SseEmitter create();
}
