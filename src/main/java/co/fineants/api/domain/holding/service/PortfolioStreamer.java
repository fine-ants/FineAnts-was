package co.fineants.api.domain.holding.service;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import reactor.core.publisher.Flux;

public interface PortfolioStreamer {
	Flux<PortfolioHoldingsRealTimeResponse> streamReturns(Long portfolioId);
}
