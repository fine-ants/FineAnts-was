package co.fineants.api.domain.holding.service;

import java.time.Duration;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ReactivePortfolioStreamingService implements PortfolioStreamingService {

	private final PortfolioHoldingService portfolioHoldingService;
	private final Duration interval;
	private final int maxCount;

	public ReactivePortfolioStreamingService(PortfolioHoldingService portfolioHoldingService, int second,
		int maxCount) {
		this.portfolioHoldingService = portfolioHoldingService;
		this.interval = Duration.ofSeconds(second);
		this.maxCount = maxCount;
	}

	@Override
	public Flux<PortfolioHoldingsRealTimeResponse> streamPortfolioReturns(Long portfolioId) {
		return Flux.interval(interval)
			.take(maxCount)
			.publishOn(Schedulers.boundedElastic())
			.map(i -> portfolioHoldingService.readMyPortfolioStocksInRealTime(portfolioId));
	}
}
