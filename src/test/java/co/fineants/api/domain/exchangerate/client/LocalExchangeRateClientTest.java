package co.fineants.api.domain.exchangerate.client;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocalExchangeRateClientTest {

	private ExchangeRateClient client;

	@BeforeEach
	void setUp() {
		client = new LocalExchangeRateClient();
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
}
