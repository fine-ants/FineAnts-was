package co.fineants.api.domain.holding.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PortfolioReturnsService {
	void streamReturns(Long portfolioId, SseEmitter emitter);
}
