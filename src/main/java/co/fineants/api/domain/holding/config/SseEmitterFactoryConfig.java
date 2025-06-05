package co.fineants.api.domain.holding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.holding.domain.factory.PortfolioSseEmitterFactory;

@Configuration
public class SseEmitterFactoryConfig {

	@Bean
	public PortfolioSseEmitterFactory portfolioSseEmitterFactory(@Value("${portfolio.timeout:30000}") long timeout) {
		return new PortfolioSseEmitterFactory(timeout);
	}
}
