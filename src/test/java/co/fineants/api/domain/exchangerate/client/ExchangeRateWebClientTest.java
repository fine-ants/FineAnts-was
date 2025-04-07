package co.fineants.api.domain.exchangerate.client;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

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

	@DisplayName("base가 주어지고 base 기준 환율들을 조회하면 환율을 조회한다")
	@Test
	void fetchRates() {
		// given
		String base = "KRW";
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=" + base.toUpperCase();
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");

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

	@DisplayName("base가 주어지고 외부 API로부터 에러 응답이 오면 빈 맵을 반환한다")
	@ParameterizedTest
	@MethodSource(value = "errorResponseProvider")
	void fetchRates_ResourceExhausted(ExchangeRateFetchResponse response) {
		// given
		String base = "KRW";
		String uri = "https://exchange-rate-api1.p.rapidapi.com/latest?base=" + base.toUpperCase();
		MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
		header.add("X-RapidAPI-Key", key);
		header.add("X-RapidAPI-Host", "exchange-rate-api1.p.rapidapi.com");

		BDDMockito.given(webClient.get(uri, header, ExchangeRateFetchResponse.class))
			.willReturn(Mono.just(response));
		// when
		Map<String, Double> actual = exchangeRateWebClient.fetchRates("KRW");
		// then
		assertThat(actual).isEmpty();
	}
}
