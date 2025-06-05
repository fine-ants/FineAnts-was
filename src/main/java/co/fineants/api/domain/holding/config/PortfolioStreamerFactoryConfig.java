package co.fineants.api.domain.holding.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.domain.factory.MarketStatusBasedPortfolioStreamerFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamMessageConsumerFactory;
import co.fineants.api.domain.holding.service.streamer.ClosedMarketPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.FluxIntervalPortfolioStreamer;
import co.fineants.api.domain.holding.service.streamer.PortfolioStreamer;
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

	@Bean
	public PortfolioStreamMessageConsumerFactory portfolioStreamMessageConsumerFactory(
		@Value("${portfolio.reconnectTimeMillis:3000}") long reconnectTimeMillis) {
		return new PortfolioStreamMessageConsumerFactory(reconnectTimeMillis);
	}
}
