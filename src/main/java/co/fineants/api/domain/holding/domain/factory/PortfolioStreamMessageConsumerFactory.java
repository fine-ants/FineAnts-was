package co.fineants.api.domain.holding.domain.factory;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.service.PortfolioStreamMessageSseSender;

public class PortfolioStreamMessageConsumerFactory {
	public PortfolioStreamMessageSseSender portfolioStreamMessageSseSender(SseEmitter emitter) {
		return new PortfolioStreamMessageSseSender(emitter);
	}
}
