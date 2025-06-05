package co.fineants.api.domain.holding.service.streamer;

import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.PortfolioCompleteStreamMessage;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusChecker;
import reactor.core.publisher.Flux;

public class ClosedMarketPortfolioStreamer implements PortfolioStreamer {

	private final MarketStatusChecker marketStatusChecker;

	public ClosedMarketPortfolioStreamer(MarketStatusChecker marketStatusChecker) {
		this.marketStatusChecker = marketStatusChecker;
	}

	@Override
	public Flux<StreamMessage> streamMessages(Long portfolioId) {
		return Flux.just(new PortfolioCompleteStreamMessage());
	}

	@Override
	public boolean supports(LocalDateTime time) {
		return !marketStatusChecker.isOpen(time);
	}
}
