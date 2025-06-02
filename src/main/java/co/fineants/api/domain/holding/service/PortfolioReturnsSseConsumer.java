package co.fineants.api.domain.holding.service;

import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortfolioReturnsSseConsumer implements Consumer<PortfolioHoldingsRealTimeResponse> {

	private final SseEmitter emitter;

	public PortfolioReturnsSseConsumer(SseEmitter emitter) {
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
			log.error("Error sending data to SseEmitter: {}", exception.getMessage(), exception);
			emitter.completeWithError(exception);
		}
	}
}
