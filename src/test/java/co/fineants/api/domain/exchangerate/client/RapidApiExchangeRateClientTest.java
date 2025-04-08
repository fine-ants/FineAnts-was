package co.fineants.api.domain.exchangerate.client;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import reactor.core.publisher.Mono;

class RapidApiExchangeRateClientTest {

	private RapidApiExchangeRateClient rapidApiExchangeRateClient;

	private WebClientHelper webClient;

	private String key;

	public static Stream<Arguments> errorResponseProvider() {
		return Stream.of(
			Arguments.of(ExchangeRateFetchResponse.invalidApiKey()),
			Arguments.of(ExchangeRateFetchResponse.requestExceeded()),
			Arguments.of(ExchangeRateFetchResponse.invalidSign()),
			Arguments.of(ExchangeRateFetchResponse.invalidCurrencyCode()),
			Arguments.of(ExchangeRateFetchResponse.networkAnomaly()),
			Arguments.of(ExchangeRateFetchResponse.queryFailed())
		);
	}

	@BeforeEach
	void setUp() {
		this.webClient = Mockito.mock(WebClientHelper.class);
		this.key = "test-key";
		rapidApiExchangeRateClient = new RapidApiExchangeRateClient(webClient, key, Duration.ofMillis(100));
	}

	@DisplayName("base와 code가 주어지고 외부 API에 code에 대한 환율이 존재하면 환율을 조회한다")
	@Test
	void fetchRateBy() {
		// given
		String base = "KRW";
		String code = "USD";
		String uri = createUri(base);
		BDDMockito.given(webClient.get(uri, base, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(ExchangeRateFetchResponse.krw(Map.of("KRW", 1.0, "USD", 0.0006861))));
		// when
		Double actual = rapidApiExchangeRateClient.fetchRateBy(code, base);
		// then
		Double expected = 0.0006861;
		assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("base와 code가 주어지고 외부 API에 에러 응답이 오면 예외를 던진다")
	@ParameterizedTest
	@MethodSource(value = "errorResponseProvider")
	void fetchRateBy_whenErrorResponse_thenThrowException(ExchangeRateFetchResponse response) {
		// given
		String base = "KRW";
		String code = "USD";
		String uri = createUri(base);
		BDDMockito.given(webClient.get(uri, base, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(response));
		// when
		Throwable throwable = catchThrowable(() -> rapidApiExchangeRateClient.fetchRateBy(code, base));
		// then
		assertThat(throwable)
			.isInstanceOf(ExternalApiGetRequestException.class)
			.hasMessage("code=%s, base=%s".formatted(code, base));
	}

	@DisplayName("base가 주어지고 base 기준 환율들을 조회하면 환율을 조회한다")
	@Test
	void fetchRates() {
		// given
		String base = "KRW";
		String uri = createUri(base);

		BDDMockito.given(webClient.get(uri, base, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(ExchangeRateFetchResponse.krw(Map.of("KRW", 1.0, "USD", 0.0006861))));
		// when
		Map<String, Double> actual = rapidApiExchangeRateClient.fetchRates(base);
		// then
		Map<String, Double> expected = Map.of("KRW", 1.0, "USD", 0.0006861);
		assertThat(actual)
			.usingRecursiveComparison()
			.isEqualTo(expected);
	}

	@NotNull
	private static String createUri(String base) {
		return "latest";
	}

	@DisplayName("base가 주어지고 외부 API로부터 에러 응답이 오면 빈 맵을 반환한다")
	@ParameterizedTest
	@MethodSource(value = "errorResponseProvider")
	void fetchRates_ResourceExhausted(ExchangeRateFetchResponse response) {
		// given
		String base = "KRW";
		String uri = createUri(base);

		BDDMockito.given(webClient.get(uri, base, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(response));
		// when
		Map<String, Double> actual = rapidApiExchangeRateClient.fetchRates("KRW");
		// then
		assertThat(actual).isEmpty();
	}
}
