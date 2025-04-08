package co.fineants.api.domain.exchangerate.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.domain.exchangerate.client.ExchangeRateClient;
import co.fineants.api.domain.exchangerate.client.LocalExchangeRateClient;
import lombok.RequiredArgsConstructor;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalExchangeRateConfig {

	private final ObjectMapper objectMapper;

	@Bean
	public ExchangeRateClient localExchangeRateClient() {
		ClassPathResource resource = new ClassPathResource("local/exchange-rate.json");
		try (InputStream inputStream = resource.getInputStream()) {
			Map<String, Double> rates = objectMapper.readValue(inputStream, new TypeReference<>() {
			});
			return new LocalExchangeRateClient(rates);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load local exchange rates", e);
		}
	}
}
