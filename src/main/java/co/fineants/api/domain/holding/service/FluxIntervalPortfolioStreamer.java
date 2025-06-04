package co.fineants.api.domain.holding.service;

import java.time.Duration;
import java.time.LocalDateTime;

import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import co.fineants.api.global.common.time.LocalDateTimeService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class FluxIntervalPortfolioStreamer implements PortfolioStreamer {

	private final PortfolioHoldingService portfolioHoldingService;
	private final StockMarketChecker stockMarketChecker;
	private final LocalDateTimeService localDateTimeService;
	private final Duration interval;
	private final int maxCount;

	public FluxIntervalPortfolioStreamer(PortfolioHoldingService portfolioHoldingService,
		StockMarketChecker stockMarketChecker, LocalDateTimeService localDateTimeService, int second, int maxCount) {
		this.portfolioHoldingService = portfolioHoldingService;
		this.stockMarketChecker = stockMarketChecker;
		this.localDateTimeService = localDateTimeService;
		this.interval = Duration.ofSeconds(second);
		this.maxCount = maxCount;
	}

	@Override
	public Flux<PortfolioHoldingsRealTimeResponse> streamReturns(Long portfolioId) {
		LocalDateTime nowDateTime = localDateTimeService.getLocalDateTimeWithNow();
		return Flux.interval(interval)
			.take(maxCount)
			.filter(i -> stockMarketChecker.isMarketOpen(nowDateTime))
			.publishOn(Schedulers.boundedElastic())
			.map(i -> portfolioHoldingService.readMyPortfolioStocksInRealTime(portfolioId));
	}
}
