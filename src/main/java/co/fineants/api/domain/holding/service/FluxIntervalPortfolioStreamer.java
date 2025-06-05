package co.fineants.api.domain.holding.service;

import java.time.Duration;
import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class FluxIntervalPortfolioStreamer implements PortfolioStreamer {

	private final PortfolioHoldingService portfolioHoldingService;
	private final StockMarketChecker stockMarketChecker;
	private final Duration interval;
	private final long maxCount;

	public FluxIntervalPortfolioStreamer(PortfolioHoldingService portfolioHoldingService,
		StockMarketChecker stockMarketChecker, long second, long maxCount) {
		this.portfolioHoldingService = portfolioHoldingService;
		this.stockMarketChecker = stockMarketChecker;
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
		return stockMarketChecker.isMarketOpen(time);
	}
}
