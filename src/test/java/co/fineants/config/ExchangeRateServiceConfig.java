package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import co.fineants.api.domain.exchangerate.client.ExchangeRateWebClient;
import co.fineants.api.domain.exchangerate.repository.ExchangeRateRepository;
import co.fineants.api.domain.exchangerate.service.ExchangeRateService;
import co.fineants.api.domain.exchangerate.service.ExchangeRateUpdateService;
import lombok.RequiredArgsConstructor;

@TestConfiguration
@RequiredArgsConstructor
public class ExchangeRateServiceConfig {

	private final ExchangeRateRepository exchangeRateRepository;

	@Bean
	public ExchangeRateService exchangeRateService(ExchangeRateWebClient mockedExchangeRateWebClient) {
		ExchangeRateUpdateService exchangeRateUpdateService = exchangeRateUpdateService(mockedExchangeRateWebClient);
		return new ExchangeRateService(
			exchangeRateRepository,
			mockedExchangeRateWebClient,
			exchangeRateUpdateService
		);
	}

	@Bean
	public ExchangeRateUpdateService exchangeRateUpdateService(ExchangeRateWebClient mockedExchangeRateWebClient) {
		return new ExchangeRateUpdateService(
			exchangeRateRepository,
			mockedExchangeRateWebClient
		);
	}

	@Bean
	public ExchangeRateWebClient mockedExchangeRateWebClient() {
		return Mockito.mock(ExchangeRateWebClient.class);
	}
}
