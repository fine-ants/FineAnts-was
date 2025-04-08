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
	public ExchangeRateClientHelper webClientHelper(@Value("${rapid.exchange-rate.key}") String key) {
		WebClient webClient = WebClient.builder()
			.baseUrl("https://exchange-rate-api1.p.rapidapi.com")
			.defaultHeaders(header -> {
				header.add("X-RapidAPI-Key", key);
				header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
			})
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
			.build();
		return new ExchangeRateClientHelper(webClient);
	}

	@Bean
	@Primary
	public ExchangeRateClient rapidApiExchangeRateClient(ExchangeRateClientHelper webClient) {
		return new RapidApiExchangeRateClient(webClient, Duration.ofSeconds(5));
	}
}
