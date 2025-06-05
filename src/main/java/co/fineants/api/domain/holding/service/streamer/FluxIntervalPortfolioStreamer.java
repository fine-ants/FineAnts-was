package co.fineants.api.domain.holding.service.streamer;

import java.time.Duration;
import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusChecker;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class FluxIntervalPortfolioStreamer implements PortfolioStreamer {

	private final PortfolioHoldingService portfolioHoldingService;
	private final MarketStatusChecker marketStatusChecker;
	private final Duration interval;
	private final long maxCount;

	public FluxIntervalPortfolioStreamer(PortfolioHoldingService portfolioHoldingService,
		MarketStatusChecker marketStatusChecker, long second, long maxCount) {
		this.portfolioHoldingService = portfolioHoldingService;
		this.marketStatusChecker = marketStatusChecker;
		this.interval = Duration.ofSeconds(second);
		this.maxCount = maxCount;
	}

	@Override
	public Flux<StreamMessage> streamMessages(Long portfolioId) {
		return Flux.interval(interval)
			.take(maxCount)
			.publishOn(Schedulers.boundedElastic())
			.map(i -> portfolioHoldingService.getPortfolioReturns(portfolioId));
	}

	@Override
	public boolean supports(LocalDateTime time) {
		return marketStatusChecker.isOpen(time);
	}
}
