package co.fineants.api.domain.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.portfolio.domain.factory.PortfolioObserverFactory;
import co.fineants.api.domain.portfolio.domain.factory.StockMarketObserverFactory;

@Configuration
public class ObserverFactoryConfig {
	@Bean
	public PortfolioObserverFactory portfolioObserverFactory(
		@Value("${portfolio.reconnectTimeMillis:3000}") long reconnectTimeMillis) {
		return new PortfolioObserverFactory(reconnectTimeMillis);
	}

	@Bean
	public StockMarketObserverFactory stockMarketObserverFactory(
		@Value("${stockMarket.reconnectTimeMillis:3000}") long reconnectTimeMillis) {
		return new StockMarketObserverFactory(reconnectTimeMillis);
	}
}
