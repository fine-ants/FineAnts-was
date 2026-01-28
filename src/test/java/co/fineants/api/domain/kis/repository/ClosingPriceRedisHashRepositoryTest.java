package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;

class ClosingPriceRedisHashRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private ClosingPriceRedisHashRepository repository;

	@DisplayName("savePrice - TickerSymbol과 Price를 받아서 종가를 저장한다.")
	@Test
	void savePrice_TickerSymbolAndPrice_SaveClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long price = 15000L;

		// when
		repository.savePrice(tickerSymbol, price);

		// then
		Assertions.assertThat(repository.fetchPrice(tickerSymbol).orElseThrow())
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", price);
	}

	@DisplayName("savePrice - KisClosingPrice를 받아서 종가를 저장한다.")
	@Test
	void savePrice_KisClosingPrice_SaveClosingPrice() {
		// given
		String tickerSymbol = "000660";
		long price = 80000L;
		KisClosingPrice kisClosingPrice = KisClosingPrice.create(tickerSymbol, price);

		// when
		repository.savePrice(kisClosingPrice);

		// then
		Assertions.assertThat(repository.fetchPrice(tickerSymbol).orElseThrow())
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", price);
	}
}
