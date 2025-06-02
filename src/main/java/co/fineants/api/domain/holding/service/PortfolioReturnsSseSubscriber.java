package co.fineants.api.domain.holding.service;

import java.util.UUID;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;

public class PortfolioReturnsSseSubscriber implements PortfolioReturnsSubscriber {

	private final SseEmitter emitter;

	public PortfolioReturnsSseSubscriber(SseEmitter emitter) {
		this.emitter = emitter;
	}

	@Override
	public void accept(PortfolioHoldingsRealTimeResponse data) {
		String id = UUID.randomUUID().toString();
		SseEmitter.SseEventBuilder builder = SseEmitter.event()
			.id(id)
			.data(data)
			.name("portfolioDetails")
			.reconnectTime(3000L);
		try {
			emitter.send(builder);
		} catch (Exception exception) {
			emitter.completeWithError(exception);
		}
	}
}
