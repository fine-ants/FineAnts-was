package co.fineants.api.domain.exchangerate.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import co.fineants.api.global.errors.exception.business.ExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import co.fineants.api.global.errors.exception.business.NetworkAnomalyExchangeRateRapidApiRequestException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Profile(value = {"production", "release"})
public class RapidApiExchangeRateClient implements ExchangeRateClient {

	private final WebClientHelper webClient;
	private final Duration timeout;

	public RapidApiExchangeRateClient(WebClientHelper webClient, Duration timeout) {
		this.webClient = webClient;
		this.timeout = timeout;
	}

	@Override
	public Double fetchRateBy(String code, String base) throws ExternalApiGetRequestException {
		String path = "latest";
		Map<String, String> queryParams = Map.of("base", base);
		try {
			return webClient.get(path, queryParams, ExchangeRateFetchResponse.class)
				.flatMap(response -> response.isSuccess() ? Mono.just(response) : Mono.error(response.toException()))
				.filter(response -> response.containsBy(code))
				.map(response -> response.getBy(code))
				.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(1))
					.filter(NetworkAnomalyExchangeRateRapidApiRequestException.class::isInstance))
				.onErrorResume(ExchangeRateRapidApiRequestException.class::isInstance, throwable -> Mono.empty())
				.blockOptional(timeout)
				.orElseThrow(() -> new ExternalApiGetRequestException("code=%s, base=%s".formatted(code, base),
					HttpStatus.BAD_REQUEST));
		} catch (IllegalStateException e) {
			throw new ExternalApiGetRequestException("code=%s, base=%s".formatted(code, base), HttpStatus.BAD_REQUEST,
				e);
		}
	}

	@Override
	public Map<String, Double> fetchRates(String base) {
		String path = "latest";
		Map<String, String> queryParams = Map.of("base", base);
		try {
			return webClient.get(path, queryParams, ExchangeRateFetchResponse.class)
				.flatMap(response -> response.isSuccess() ? Mono.just(response) : Mono.error(response.toException()))
				.map(ExchangeRateFetchResponse::getRates)
				.retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(1))
					.filter(NetworkAnomalyExchangeRateRapidApiRequestException.class::isInstance))
				.onErrorResume(ExchangeRateRapidApiRequestException.class::isInstance, throwable -> Mono.empty())
				.blockOptional(timeout)
				.orElse(Collections.emptyMap());
		} catch (IllegalStateException e) {
			log.warn("RapidApiExchangeRateClient fetchRates error", e);
			return Collections.emptyMap();
		}
	}
}
