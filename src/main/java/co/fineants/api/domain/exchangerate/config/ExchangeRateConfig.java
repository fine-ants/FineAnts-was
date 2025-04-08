package co.fineants.api.domain.exchangerate.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.RapidApiExchangeRateClient;
import co.fineants.api.domain.member.service.WebClientWrapper;

@Configuration
public class ExchangeRateConfig {

	private final WebClientWrapper webClient;
	private final String key;

	public ExchangeRateConfig(WebClientWrapper webClient, @Value("${rapid.exchange-rate.key}") String key) {
		this.webClient = webClient;
		this.key = key;
	}

	@Bean
	public ExchangeRateClient exchangeRateWebClient() {
		return new RapidApiExchangeRateClient(webClient, key, Duration.ofSeconds(5));
	}
}
