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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import co.fineants.api.domain.exchangerate.domain.dto.response.ExchangeRateFetchResponse;
import co.fineants.api.domain.member.service.WebClientWrapper;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;
import reactor.core.publisher.Mono;

class ExchangeRateWebClientTest {

	private ExchangeRateWebClient exchangeRateWebClient;

	private WebClientWrapper webClient;

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
		this.webClient = Mockito.mock(WebClientWrapper.class);
		this.key = "test-key";
		exchangeRateWebClient = new ExchangeRateWebClient(webClient, key, Duration.ofMillis(100));
	}

	@DisplayName("base와 code가 주어지고 외부 API에 code에 대한 환율이 존재하면 환율을 조회한다")
	@Test
	void fetchRateBy() {
		// given
		String base = "KRW";
		String code = "USD";
		String uri = createUri(base);
		MultiValueMap<String, String> header = createHeader();
		BDDMockito.given(webClient.get(uri, header, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(ExchangeRateFetchResponse.krw(Map.of("KRW", 1.0, "USD", 0.0006861))));
		// when
		Double actual = exchangeRateWebClient.fetchRateBy(code, base);
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
		MultiValueMap<String, String> header = createHeader();
		BDDMockito.given(webClient.get(uri, header, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(response));
		// when
		Throwable throwable = catchThrowable(() -> exchangeRateWebClient.fetchRateBy(code, base));
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
		MultiValueMap<String, String> header = createHeader();

		BDDMockito.given(webClient.get(uri, header, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(ExchangeRateFetchResponse.krw(Map.of("KRW", 1.0, "USD", 0.0006861))));
		// when
		Map<String, Double> actual = exchangeRateWebClient.fetchRates("KRW");
		// then
		Map<String, Double> expected = Map.of("KRW", 1.0, "USD", 0.0006861);
		assertThat(actual)
			.usingRecursiveComparison()
			.isEqualTo(expected);
	}

	@NotNull
	private static String createUri(String base) {
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=" + base.toUpperCase();
		return uri;
	}

	@NotNull
	private MultiValueMap<String, String> createHeader() {
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");
		return header;
	}

	@DisplayName("base가 주어지고 외부 API로부터 에러 응답이 오면 빈 맵을 반환한다")
	@ParameterizedTest
	@MethodSource(value = "errorResponseProvider")
	void fetchRates_ResourceExhausted(ExchangeRateFetchResponse response) {
		// given
		String base = "KRW";
		String uri = createUri(base);
		MultiValueMap<String, String> header = createHeader();

		BDDMockito.given(webClient.get(uri, header, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(response));
		// when
		Map<String, Double> actual = exchangeRateWebClient.fetchRates("KRW");
		// then
		assertThat(actual).isEmpty();
	}
}
