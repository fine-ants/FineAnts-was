package co.fineants.api.domain.holding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.market_status_checker.MarketStatusChecker;
import co.fineants.api.domain.holding.service.streamer.AlwaysOpenPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.ClosedMarketPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.FluxIntervalPortfolioStreamer;

@Configuration
public class PortfolioStreamerConfig {
	@Bean
	public FluxIntervalPortfolioStreamer fluxIntervalPortfolioStreamer(PortfolioHoldingService service,
		MarketStatusChecker koreanMarketStatusChecker) {
		int second = 5;
		int maxCount = 6;
		return new FluxIntervalPortfolioStreamer(service, koreanMarketStatusChecker, second, maxCount);
	}

	@Bean
	public ClosedMarketPortfolioStreamer closedMarketPortfolioStreamer(MarketStatusChecker koreanMarketStatusChecker) {
		return new ClosedMarketPortfolioStreamer(koreanMarketStatusChecker);
	}

	@Bean
	public AlwaysOpenPortfolioStreamer alwaysOpenPortfolioStreamer(PortfolioHoldingService service) {
		int second = 5;
		int maxCount = 6;
		return new AlwaysOpenPortfolioStreamer(service, second, maxCount);
	}
}
