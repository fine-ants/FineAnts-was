package co.fineants.api.domain.exchangerate.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.RapidApiExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.WebClientHelper;

@Configuration
@Profile(value = {"production", "release"})
public class RealExchangeRateConfig {

	@Value("${rapid.exchange-rate.key}")
	private String key;

	@Bean
	public WebClientHelper webClientHelper() {
		WebClient webClient = WebClient.builder()
			.baseUrl("https://exchange-rate-api1.p.rapidapi.com")
			.defaultHeaders(header -> {
				header.add("X-RapidAPI-Key", key);
				header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
			})
			.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
			.build();
		return new WebClientHelper(webClient);
	}

	@Bean
	public ExchangeRateClient rapidApiExchangeRateClient(WebClientHelper webClient) {
		return new RapidApiExchangeRateClient(webClient, key, Duration.ofSeconds(5));
	}
}
