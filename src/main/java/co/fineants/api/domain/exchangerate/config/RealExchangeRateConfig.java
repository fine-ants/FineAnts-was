package co.fineants.api.domain.exchangerate.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.RapidApiExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.WebClientWrapper;

@Configuration
@Profile(value = {"production", "release"})
public class RealExchangeRateConfig {

	private final String key;

	public RealExchangeRateConfig(@Value("${rapid.exchange-rate.key}") String key) {
		this.key = key;
	}

	@Bean
	public ExchangeRateClient rapidApiExchangeRateClient(WebClientWrapper webClient) {
		return new RapidApiExchangeRateClient(webClient, key, Duration.ofSeconds(5));
	}
}
