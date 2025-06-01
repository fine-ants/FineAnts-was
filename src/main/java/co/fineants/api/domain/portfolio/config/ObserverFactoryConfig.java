package co.fineants.api.domain.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.portfolio.domain.factory.PortfolioObserverFactory;

@Configuration
public class ObserverFactoryConfig {
	@Bean
	public PortfolioObserverFactory portfolioObserverFactory(
		@Value("${portfolio.reconnectTimeMillis:3000}") long reconnectTimeMillis) {
		return new PortfolioObserverFactory(reconnectTimeMillis);
	}
}
