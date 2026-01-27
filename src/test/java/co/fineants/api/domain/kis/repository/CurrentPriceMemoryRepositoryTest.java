package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
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

	@DisplayName("savePrice - KisCurrentPrice 배열로 현재가 저장")
	@Test
	void savePrice_withKisCurrentPriceArray() {
		// given
		String tickerSymbol1 = "005930";
		long price1 = 60000L;
		String tickerSymbol2 = "000660";
		long price2 = 90000L;

		// when
		currentPriceMemoryRepository.savePrice(
			KisCurrentPrice.create(tickerSymbol1, price1),
			KisCurrentPrice.create(tickerSymbol2, price2)
		);

		// then
		CurrentPriceRedisEntity actual1 = currentPriceMemoryRepository.fetchPriceBy(tickerSymbol1).orElseThrow();
		Assertions.assertThat(actual1)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol1)
			.hasFieldOrPropertyWithValue("price", price1);

		CurrentPriceRedisEntity actual2 = currentPriceMemoryRepository.fetchPriceBy(tickerSymbol2).orElseThrow();
		Assertions.assertThat(actual2)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol2)
			.hasFieldOrPropertyWithValue("price", price2);
	}

	@DisplayName("fetchPriceBy - 존재하지 않는 티커 심볼로 조회 시 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenTickerSymbolDoesNotExist_thenReturnEmptyOptional() {
		// given
		String tickerSymbol = "000000";
		// when
		Optional<CurrentPriceRedisEntity> result = currentPriceMemoryRepository.fetchPriceBy(tickerSymbol);
		// then
		Assertions.assertThat(result).isEmpty();
	}

	@DisplayName("fetchPriceBy - 티커 심볼이 유효하지 않을때 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenTickerSymbolIsInvalid_thenReturnEmptyOptional() {
		// when
		Optional<CurrentPriceRedisEntity> result1 = currentPriceMemoryRepository.fetchPriceBy(null);
		Optional<CurrentPriceRedisEntity> result2 = currentPriceMemoryRepository.fetchPriceBy("");
		Optional<CurrentPriceRedisEntity> result3 = currentPriceMemoryRepository.fetchPriceBy(" ");
		Optional<CurrentPriceRedisEntity> result4 = currentPriceMemoryRepository.fetchPriceBy("  ");
		// then
		Assertions.assertThat(result1).isEmpty();
		Assertions.assertThat(result2).isEmpty();
		Assertions.assertThat(result3).isEmpty();
		Assertions.assertThat(result4).isEmpty();
	}

	@DisplayName("fetchPriceBy - 티커 심볼로 현재가 조회")
	@Test
	void fetchPriceBy() {
		// given
		String tickerSymbol = "005930";
		long price = 50000L;
		currentPriceMemoryRepository.savePrice(tickerSymbol, price);

		// when
		Optional<CurrentPriceRedisEntity> entity = currentPriceMemoryRepository.fetchPriceBy(tickerSymbol);

		// then
		Assertions.assertThat(entity).isPresent();
		CurrentPriceRedisEntity actual = entity.orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", price);
	}
}
