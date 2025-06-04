package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import reactor.core.publisher.Flux;

public class ClosedMarketPortfolioStreamer implements PortfolioStreamer {

	private final StockMarketChecker stockMarketChecker;

	public ClosedMarketPortfolioStreamer(StockMarketChecker stockMarketChecker) {
		this.stockMarketChecker = stockMarketChecker;
	}

	@Override
	public Flux<PortfolioHoldingsRealTimeResponse> streamReturns(Long portfolioId) {
		return Flux.empty();
	}

	@Override
	public boolean supports(LocalDateTime time) {
		return !stockMarketChecker.isMarketOpen(time);
	}
}
