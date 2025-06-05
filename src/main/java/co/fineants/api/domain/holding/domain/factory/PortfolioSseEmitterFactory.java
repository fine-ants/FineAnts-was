package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortfolioSseEmitterFactory implements SseEmitterFactory {

	private final long timeout;

	public PortfolioSseEmitterFactory(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public SseEmitter create() {
		SseEmitter emitter = new SseEmitter(timeout);
		emitter.onTimeout(() -> {
			log.info("SseEmitter timeout, removing emitter");
			emitter.complete();
		});
		emitter.onCompletion(() -> {
			log.info("SseEmitter completed, removing emitter");
		});
		emitter.onError(throwable -> {
			log.error("SseEmitter error: {}", throwable.getMessage(), throwable);
			emitter.completeWithError(throwable);
		});
		return emitter;
	}
}
