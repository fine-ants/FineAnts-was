package co.fineants.api.domain.holding.service.streamer;

import java.time.Duration;
import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class AlwaysOpenPortfolioStreamer implements PortfolioStreamer {
	private final PortfolioHoldingService portfolioHoldingService;
	private final Duration interval;
	private final long maxCount;

	public AlwaysOpenPortfolioStreamer(PortfolioHoldingService portfolioHoldingService, long second,
		long maxCount) {
		this.portfolioHoldingService = portfolioHoldingService;
		this.interval = Duration.ofSeconds(second);
		this.maxCount = maxCount;
		if (this.maxCount < 0) {
			throw new IllegalArgumentException("maxCount must be non-negative");
		}
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
		return true;
	}
}
