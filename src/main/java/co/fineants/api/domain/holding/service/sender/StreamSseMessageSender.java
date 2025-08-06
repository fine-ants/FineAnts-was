package co.fineants.api.domain.holding.service.sender;

import java.util.function.Consumer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface StreamSseMessageSender extends Consumer<SseEmitter.SseEventBuilder> {

	@Override
	void accept(SseEmitter.SseEventBuilder builder);
}
