package co.fineants.api.domain.holding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.service.MarketStatusChecker;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.streamer.ClosedMarketPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.FluxIntervalPortfolioStreamer;

@Configuration
public class PortfolioStreamerConfig {
	@Bean
	public FluxIntervalPortfolioStreamer fluxIntervalPortfolioStreamer(PortfolioHoldingService service,
		MarketStatusChecker stockMarketChecker) {
		int second = 5;
		int maxCount = 6;
		return new FluxIntervalPortfolioStreamer(service, stockMarketChecker, second, maxCount);
	}

	@Bean
	public ClosedMarketPortfolioStreamer closedMarketPortfolioStreamer(MarketStatusChecker stockMarketChecker) {
		return new ClosedMarketPortfolioStreamer(stockMarketChecker);
	}
}
