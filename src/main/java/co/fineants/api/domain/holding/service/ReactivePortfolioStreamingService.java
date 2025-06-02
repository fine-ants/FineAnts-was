package co.fineants.api.domain.holding.service;

import java.time.Duration;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ReactivePortfolioStreamingService implements PortfolioStreamingService {

	private final PortfolioHoldingService portfolioHoldingService;

	public ReactivePortfolioStreamingService(PortfolioHoldingService portfolioHoldingService) {
		this.portfolioHoldingService = portfolioHoldingService;
	}

	@Override
	public Flux<PortfolioHoldingsRealTimeResponse> streamPortfolioReturns(Long portfolioId) {
		return Flux.interval(Duration.ofSeconds(5))
			.take(6)
			.publishOn(Schedulers.boundedElastic())
			.map(i -> portfolioHoldingService.readMyPortfolioStocksInRealTime(portfolioId));
	}
}
