package co.fineants.api.domain.exchangerate.client;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocalExchangeRateClientTest {

	private ExchangeRateClient client;

	@BeforeEach
	void setUp() {
		Map<String, Double> rates = Map.of(
			"USD", 1.0,
			"KRW", 1500.0
		);
		client = new LocalExchangeRateClient(rates);
	}

	@DisplayName("base가 KRW인 상태에서 USD의 환율을 조회한다")
	@Test
	void fetchRateBy() {
		// given

		// when
		Double actual = client.fetchRateBy("USD", "KRW");
		// then
		Double expected = 1.0 / 1500.0;
		assertThat(actual)
			.usingRecursiveComparison()
			.isEqualTo(expected);
	}

	@DisplayName("base가 KRW인 상태에서 환율들을 조회한다")
	@Test
	void fetchRates() {
		// given
		String base = "KRW";
		// when
		Map<String, Double> actual = client.fetchRates(base);
		// then
		Map<String, Double> expected = Map.of(
			"KRW", 1.0,
			"USD", 1.0 / 1500.0
		);
		assertThat(actual)
			.usingRecursiveComparison()
			.isEqualTo(expected);
	}
}
