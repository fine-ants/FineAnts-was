package co.fineants.api.domain.holding.service;

import java.util.function.Consumer;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class PortfolioReturnsSseService implements PortfolioReturnsService {

	private final PortfolioStreamer streamer;

	public PortfolioReturnsSseService(PortfolioStreamer streamer) {
		this.streamer = streamer;
	}

	@Override
	public void streamReturns(Long portfolioId, SseEmitter emitter) {
		Consumer<PortfolioHoldingsRealTimeResponse> consumer = new PortfolioReturnsSseConsumer(emitter);
		Flux<PortfolioHoldingsRealTimeResponse> flux = streamer.streamReturns(portfolioId);
		flux.subscribe(consumer);
	}
}
