package co.fineants.api.domain.holding.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.domain.factory.MarketStatusBasedPortfolioStreamerFactory;
import co.fineants.api.domain.holding.service.ClosedMarketPortfolioStreamer;
import co.fineants.api.domain.holding.service.FluxIntervalPortfolioStreamer;
import co.fineants.api.domain.holding.service.PortfolioStreamer;
import co.fineants.api.global.common.time.LocalDateTimeService;

@Configuration
public class PortfolioStreamerFactoryConfig {

	@Bean
	public MarketStatusBasedPortfolioStreamerFactory marketStatusBasedPortfolioStreamerFactory(
		FluxIntervalPortfolioStreamer fluxIntervalPortfolioStreamer,
		ClosedMarketPortfolioStreamer closedMarketPortfolioStreamer, LocalDateTimeService localDateTimeService) {
		List<PortfolioStreamer> streamers = List.of(fluxIntervalPortfolioStreamer, closedMarketPortfolioStreamer);
		return new MarketStatusBasedPortfolioStreamerFactory(streamers, localDateTimeService);
	}
}
