package co.fineants.api.domain.holding.service;

import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.PortfolioCompleteStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import reactor.core.publisher.Flux;

public class ClosedMarketPortfolioStreamer implements PortfolioStreamer {

	private final StockMarketChecker stockMarketChecker;

	public ClosedMarketPortfolioStreamer(StockMarketChecker stockMarketChecker) {
		this.stockMarketChecker = stockMarketChecker;
	}

	@Override
	public Flux<StreamMessage> streamMessages(Long portfolioId) {
		return Flux.just(new PortfolioCompleteStreamMessage());
	}

	@Override
	public boolean supports(LocalDateTime time) {
		return !stockMarketChecker.isMarketOpen(time);
	}
}
