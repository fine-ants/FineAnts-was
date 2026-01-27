package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;

class CurrentPriceMemoryRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceMemoryRepository currentPriceMemoryRepository;

	@Test
	void savePrice() {
		// given
		String tickerSymbol = "005930";
		long price = 60000L;

		// when
		currentPriceMemoryRepository.savePrice(tickerSymbol, price);

		// then
		CurrentPriceRedisEntity actual = currentPriceMemoryRepository.fetchPriceBy(tickerSymbol).orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", price);
	}
}
