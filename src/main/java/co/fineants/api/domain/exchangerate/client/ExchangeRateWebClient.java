package co.fineants.api.domain.exchangerate.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import co.fineants.api.domain.member.service.WebClientWrapper;
import co.fineants.api.global.errors.exception.business.ExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import co.fineants.api.global.errors.exception.business.NetworkAnomalyExchangeRateRapidApiRequestException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
public class ExchangeRateWebClient {

	private final WebClientWrapper webClient;
	private final String key;
	private final Duration timeout;

	public ExchangeRateWebClient(WebClientWrapper webClient, @Value("${rapid.exchange-rate.key}") String key,
		Duration timeout) {
		this.webClient = webClient;
		this.key = key;
		this.timeout = timeout;
	}

	public Double fetchRateBy(String code, String base) {
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=" + base.toUpperCase();
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
		return webClient.get(uri, header, ExchangeRateFetchResponse.class)
			.filter(response -> response.containsBy(code))
			.map(response -> response.getBy(code))
			.blockOptional(timeout)
			.orElseThrow(() -> new ExternalApiGetRequestException(base, HttpStatus.BAD_REQUEST));
	}

	public Map<String, Double> fetchRates(String base) {
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=" + base.toUpperCase();
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
		try {
			return webClient.get(uri, header, ExchangeRateFetchResponse.class)
				.flatMap(response -> response.isSuccess() ? Mono.just(response) : Mono.error(response.toException()))
				.map(ExchangeRateFetchResponse::getRates)
				.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(1))
					.filter(NetworkAnomalyExchangeRateRapidApiRequestException.class::isInstance))
				.onErrorResume(ExchangeRateRapidApiRequestException.class::isInstance, throwable -> Mono.empty())
				.blockOptional(timeout)
				.orElse(Collections.emptyMap());
		} catch (IllegalStateException e) {
			log.warn("ExchangeRateWebClient fetchRates error", e);
			return Collections.emptyMap();
		}
	}
}
