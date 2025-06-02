package co.fineants.api.domain.holding.service;

import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PortfolioReturnsSseService implements PortfolioReturnsService {

	private final PortfolioStreamer fluxIntervalPortfolioStreamer;

	public PortfolioReturnsSseService(PortfolioStreamer fluxIntervalPortfolioStreamer) {
		this.fluxIntervalPortfolioStreamer = fluxIntervalPortfolioStreamer;
	}

	@Override
	public void streamReturns(Long portfolioId, SseEmitter emitter) {
		Consumer<PortfolioHoldingsRealTimeResponse> consumer = new PortfolioReturnsSseConsumer(emitter);
		fluxIntervalPortfolioStreamer.streamReturns(portfolioId).subscribe(consumer);
	}
}
