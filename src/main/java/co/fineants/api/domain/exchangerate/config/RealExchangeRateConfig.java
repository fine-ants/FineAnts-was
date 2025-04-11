package co.fineants.api.domain.exchangerate.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.ExchangeRateClientHelper;
import co.fineants.api.domain.exchangerate.client.RapidApiExchangeRateClient;

@Configuration
@Profile(value = {"real", "production", "release"})
public class RealExchangeRateConfig {

	@Bean
	public ExchangeRateClientHelper webClientHelper(
		@Value("${rapid.exchange-rate.base-uri}") String baseUri,
		@Value("${rapid.exchange-rate.key}") String key,
		@Value("${rapid.exchange-rate.host}") String host,
		@Value("${rapid.exchange-rate.max-in-memory-size}") int maxInMemorySize) {
		WebClient webClient = WebClient.builder()
			.baseUrl(baseUri)
			.defaultHeaders(header -> {
				header.add("X-RapidAPI-Key", key);
				header.add("X-RapidAPI-Host", host);
			})
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
			.build();
		return new ExchangeRateClientHelper(webClient);
	}

	@Bean
	@Primary
	public ExchangeRateClient rapidApiExchangeRateClient(ExchangeRateClientHelper webClient) {
		return new RapidApiExchangeRateClient(webClient, Duration.ofSeconds(10));
	}
}
