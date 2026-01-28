package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.domain.ClosingPriceRedisEntity;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;

class ClosingPriceRedisHashRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private ClosingPriceRedisHashRepository repository;

	@Autowired
	private StringRedisTemplate template;

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

	@DisplayName("savePrice - 유효하지 않은 TickerSymbol로 종가를 저장하고자 하면 저장되지 않는다.")
	@Test
	void savePrice_InvalidTickerSymbol_DoNotSaveClosingPrice() {
		// given
		long price = 10000L;

		// when
		repository.savePrice(null, price);
		repository.savePrice("", price);
		repository.savePrice(" ", price);
		repository.savePrice("  ", price);
		// then
		Assertions.assertThat(template.opsForHash().size(ClosingPriceRedisHashRepository.KEY)).isZero();
	}

	@DisplayName("savePrice - 음수 Price로 종가를 저장하고자 하면 저장되지 않는다.")
	@Test
	void savePrice_NegativePrice_DoNotSaveClosingPrice() {
		// given
		String tickerSymbol = "005930";

		// when
		repository.savePrice(tickerSymbol, -1000L);

		// then
		Assertions.assertThat(template.opsForHash().size(ClosingPriceRedisHashRepository.KEY)).isZero();
	}

	@DisplayName("fetchPrice - 저장된 TickerSymbol로 종가를 조회한다.")
	@Test
	void fetchPrice_ExistingTickerSymbol_ReturnClosingPrice() {
		// given
		String tickerSymbol = "005930";
		long price = 15000L;
		repository.savePrice(tickerSymbol, price);

		// when
		Optional<ClosingPriceRedisEntity> actual = repository.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actual.orElseThrow())
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", price);
	}

	@DisplayName("fetchPrice - 저장되지 않은 TickerSymbol로 종가를 조회하면 빈 Optional을 반환한다.")
	@Test
	void fetchPrice_NonExistingTickerSymbol_ReturnEmptyOptional() {
		// given
		String tickerSymbol = "000660";

		// when
		Optional<ClosingPriceRedisEntity> actual = repository.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actual).isEmpty();
	}

	@DisplayName("fetchPrice - 유효하지 않은 TickerSymbol로 종가를 조회하면 빈 Optional을 반환한다.")
	@Test
	void fetchPrice_InvalidTickerSymbol_ReturnEmptyOptional() {
		// when
		Optional<ClosingPriceRedisEntity> actual1 = repository.fetchPrice(null);
		Optional<ClosingPriceRedisEntity> actual2 = repository.fetchPrice("");
		Optional<ClosingPriceRedisEntity> actual3 = repository.fetchPrice(" ");
		Optional<ClosingPriceRedisEntity> actual4 = repository.fetchPrice("  ");

		// then
		Assertions.assertThat(actual1).isEmpty();
		Assertions.assertThat(actual2).isEmpty();
		Assertions.assertThat(actual3).isEmpty();
		Assertions.assertThat(actual4).isEmpty();
	}
}

