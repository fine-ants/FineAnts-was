package co.fineants.api.domain.exchangerate.client;

import java.time.Duration;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import co.fineants.api.global.errors.exception.business.ExchangeRateRapidApiRequestException;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import co.fineants.api.global.errors.exception.business.NetworkAnomalyExchangeRateRapidApiRequestException;
import jakarta.validation.constraints.NotNull;
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

	/**
	 * base를 기준으로 한 code의 환율을 조회한다.
	 * @param code 환율 코드
	 * @param base 환율 기준 통화 코드
	 * @return 환율 정보. ex) base="USD", code="KRW", rate=1500.0
	 * @throws ExternalApiGetRequestException 외부 API로부터 환율을 조회를 실패하면 예외가 발생함
	 */
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
				.block(timeout);
		} catch (IllegalStateException e) {
			log.warn("RapidApiExchangeRateClient fetchRateBy timeout error", e);
			throw new ExternalApiGetRequestException(getErrorMessage(code, base),
				HttpStatus.SERVICE_UNAVAILABLE, e);
		} catch (ExchangeRateRapidApiRequestException e) {
			log.warn("RapidApiExchangeRateClient fetchRateBy error", e);
			throw new ExternalApiGetRequestException(getErrorMessage(code, base), e.getHttpStatus(), e);
		}
	}

	private String getErrorMessage(String code, String base) {
		return "code=%s, base=%s".formatted(code, base);
	}

	/**
	 * base를 기준으로 한 환율들을 조회한다.
	 * @param base 환율 기준 통화 코드
	 * @return 환율 정보 Map. ex) base="USD", rates={"KRW": 1500.0, "USD": 1.0}
	 * @throws ExternalApiGetRequestException 외부 API로부터 환율을 조회를 실패하면 예외가 발생함
	 */
	@Override
	public Map<String, Double> fetchRates(String base) throws ExternalApiGetRequestException {
		String path = "latest";
		Map<String, String> queryParams = Map.of("base", base);

		try {
			return webClient.get(path, queryParams)
				.flatMap(response -> response.isSuccess() ? Mono.just(response) : Mono.error(response.toException()))
				.map(ExchangeRateFetchResponse::getRates)
				.retryWhen(getRetryBackoffSpec())
				.block(timeout);
		} catch (IllegalStateException e) {
			log.warn("RapidApiExchangeRateClient fetchRates timeout error", e);
			throw new ExternalApiGetRequestException("base=%s".formatted(base), HttpStatus.SERVICE_UNAVAILABLE, e);
		} catch (ExchangeRateRapidApiRequestException e) {
			log.warn("RapidApiExchangeRateClient fetchRates error", e);
			throw new ExternalApiGetRequestException("base=%s".formatted(base), e.getHttpStatus(), e);
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
