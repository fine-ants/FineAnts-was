package co.fineants.api.domain.exchangerate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.LocalExchangeRateClient;

@Configuration
@Profile("local")
public class LocalExchangeRateConfig {

	@Bean
	public ExchangeRateClient localExchangeRateClient() {
		return new LocalExchangeRateClient();
	}
}
