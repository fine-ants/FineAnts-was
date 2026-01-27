package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;

class CurrentPriceMemoryRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceMemoryRepository currentPriceMemoryRepository;

	@DisplayName("savePrice - 티커 심볼로 현재가 저장")
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

	@DisplayName("savePrice - 티커 심볼이 유효하지 않을때 저장하지 않음")
	@Test
	void savePrice_whenTickerSymbolIsInvalid_thenDoNothing() {
		// given
		long price = 50000L;
		// when
		currentPriceMemoryRepository.savePrice((String)null, price);
		currentPriceMemoryRepository.savePrice("", price);
		currentPriceMemoryRepository.savePrice(" ", price);
		currentPriceMemoryRepository.savePrice("  ", price);
		// then
		Assertions.assertThat(currentPriceMemoryRepository.fetchPriceBy(null)).isEmpty();
		Assertions.assertThat(currentPriceMemoryRepository.fetchPriceBy("")).isEmpty();
		Assertions.assertThat(currentPriceMemoryRepository.fetchPriceBy(" ")).isEmpty();
		Assertions.assertThat(currentPriceMemoryRepository.fetchPriceBy("  ")).isEmpty();
	}
}
