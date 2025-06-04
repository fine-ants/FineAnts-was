package co.fineants.api.domain.holding.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.service.FluxIntervalPortfolioStreamer;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.holding.service.StockMarketChecker;
import co.fineants.api.global.common.time.LocalDateTimeService;

@Configuration
public class PortfolioStreamerConfig {
	@Bean
	public FluxIntervalPortfolioStreamer fluxIntervalPortfolioStreamer(PortfolioHoldingService service,
		StockMarketChecker stockMarketChecker, LocalDateTimeService localDateTimeService) {
		int second = 5;
		int maxCount = 6;
		return new FluxIntervalPortfolioStreamer(service, stockMarketChecker, localDateTimeService, second, maxCount);
	}
}
