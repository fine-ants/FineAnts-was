package co.fineants.api.domain.exchangerate.client;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import co.fineants.api.global.errors.exception.business.ExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import co.fineants.api.global.errors.exception.business.NetworkAnomalyExchangeRateRapidApiRequestException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Slf4j
@Profile(value = {"production", "release"})
public class RapidApiExchangeRateClient implements ExchangeRateClient {

	private final ExchangeRateClientHelper webClient;
	private final Duration timeout;

	public RapidApiExchangeRateClient(ExchangeRateClientHelper webClient, Duration timeout) {
		this.webClient = webClient;
		this.timeout = timeout;
	}

	@Override
	public Double fetchRateBy(String code, String base) throws ExternalApiGetRequestException {
		String path = "latest";
		Map<String, String> queryParams = Map.of("base", base);
		try {
			return webClient.get(path, queryParams)
				.flatMap(response -> response.isSuccess() ? Mono.just(response) : Mono.error(response.toException()))
				.filter(response -> response.containsBy(code))
				.map(response -> response.getBy(code))
				.retryWhen(getRetryBackoffSpec())
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
	public Map<String, Double> fetchRates(String base) throws ExternalApiGetRequestException {
		String path = "latest";
		Map<String, String> queryParams = Map.of("base", base);

		try {
			return webClient.get(path, queryParams)
				.flatMap(response -> response.isSuccess() ? Mono.just(response) : Mono.error(response.toException()))
				.map(ExchangeRateFetchResponse::getRates)
				.retryWhen(getRetryBackoffSpec())
				.blockOptional(timeout)
				.orElse(Collections.emptyMap());
		} catch (IllegalStateException e) {
			log.warn("RapidApiExchangeRateClient fetchRates timeout error", e);
			throw new ExternalApiGetRequestException("base=%s".formatted(base), HttpStatus.BAD_REQUEST, e);
		} catch (ExchangeRateRapidApiRequestException e) {
			log.warn("RapidApiExchangeRateClient fetchRates error", e);
			throw new ExternalApiGetRequestException("base=%s".formatted(base), HttpStatus.BAD_REQUEST, e);
		}
	}

	/**
	 * return The RetryBackoffSpec Object
	 * - 3 times retry : 재시도 횟수
	 * - 500ms backoff : 재시도 대기 시간, ex) 500ms, 1s, 2s
	 * - max backoff 3s : 최대 재시도 대기 시간
	 * - 20% jitter : 재시도 대기 시간에 20%의 랜덤한 시간 추가, ex) 500ms + 20% = 600ms
	 * @return RetryBackoffSpec
	 */
	@NotNull
	private RetryBackoffSpec getRetryBackoffSpec() {
		return Retry.backoff(3, Duration.ofMillis(500))
			.maxBackoff(Duration.ofSeconds(3))
			.jitter(0.2)
			.filter(NetworkAnomalyExchangeRateRapidApiRequestException.class::isInstance);
	}
}
