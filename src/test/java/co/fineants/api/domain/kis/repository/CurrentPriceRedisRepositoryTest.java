package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.stock.domain.Stock;

class CurrentPriceRedisRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@DisplayName("savePrice - 티커 심볼로 현재가 저장")
	@Test
	void savePrice_whenTickerSymbol_thenSavePrice() {
		// given
		String tickerSymbol = "005930";
		long price = 60000L;

		// when
		currentPriceRedisRepository.savePrice(tickerSymbol, price);

		// then
		CurrentPriceRedisEntity actual = currentPriceRedisRepository.fetchPriceBy(tickerSymbol).orElseThrow();
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
		currentPriceRedisRepository.savePrice((String)null, price);
		currentPriceRedisRepository.savePrice("", price);
		currentPriceRedisRepository.savePrice(" ", price);
		currentPriceRedisRepository.savePrice("  ", price);
		// then
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy(null)).isEmpty();
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy("")).isEmpty();
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy(" ")).isEmpty();
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy("  ")).isEmpty();
	}

	@DisplayName("savePrice - KisCurrentPrice 배열로 현재가 저장")
	@Test
	void savePrice_whenKisCurrentPriceArray_thenSavePrices() {
		// given
		KisCurrentPrice cp1 = KisCurrentPrice.create("005930", 60000L);
		KisCurrentPrice cp2 = KisCurrentPrice.create("000660", 80000L);
		// when
		currentPriceRedisRepository.savePrice(cp1, cp2);
		// then
		CurrentPriceRedisEntity actual1 = currentPriceRedisRepository.fetchPriceBy("005930").orElseThrow();
		CurrentPriceRedisEntity actual2 = currentPriceRedisRepository.fetchPriceBy("000660").orElseThrow();
		Assertions.assertThat(actual1)
			.hasFieldOrPropertyWithValue("tickerSymbol", "005930")
			.hasFieldOrPropertyWithValue("price", 60000L);
		Assertions.assertThat(actual2)
			.hasFieldOrPropertyWithValue("tickerSymbol", "000660")
			.hasFieldOrPropertyWithValue("price", 80000L);
	}

	@DisplayName("savePrice - KisCurrentPrice로 현재가 저장")
	@Test
	void savePrice_whenKisCurrentPrice_thenSavePrice() {
		// given
		KisCurrentPrice cp = KisCurrentPrice.create("005930", 60000L);
		// when
		currentPriceRedisRepository.savePrice(cp);
		// then
		CurrentPriceRedisEntity actual = currentPriceRedisRepository.fetchPriceBy("005930").orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", "005930")
			.hasFieldOrPropertyWithValue("price", 60000L);
	}

	@DisplayName("savePrice - Stock과 가격으로 현재가 저장")
	@Test
	void savePrice_whenStockAndPrice_thenSavePrice() {
		// given
		Stock stock = createSamsungStock();
		String tickerSymbol = stock.getTickerSymbol();
		long price = 60000L;
		// when
		currentPriceRedisRepository.savePrice(stock, price);
		// then
		CurrentPriceRedisEntity actual = currentPriceRedisRepository.fetchPriceBy(tickerSymbol).orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", price);
	}

	@DisplayName("fetchPriceBy - 저장된 현재가가 있을때 현재가를 반환")
	@Test
	void fetchPriceBy_whenPriceExists_thenReturnCurrentPrice() {
		// given
		String ticker = "005930";
		currentPriceRedisRepository.savePrice(ticker, 50000L);
		// when
		CurrentPriceRedisEntity entity = currentPriceRedisRepository.fetchPriceBy(ticker).orElseThrow();
		// then
		Assertions.assertThat(entity)
			.hasFieldOrPropertyWithValue("tickerSymbol", ticker)
			.hasFieldOrPropertyWithValue("price", 50000L);
	}

	@DisplayName("fetchPriceBy - 티커 심볼이 유효하지 않을때 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenTickerSymbolIsInvalid_thenReturnEmptyOptional() {
		// given

		// when
		Optional<CurrentPriceRedisEntity> result1 = currentPriceRedisRepository.fetchPriceBy(null);
		Optional<CurrentPriceRedisEntity> result2 = currentPriceRedisRepository.fetchPriceBy("");
		Optional<CurrentPriceRedisEntity> result3 = currentPriceRedisRepository.fetchPriceBy(" ");
		Optional<CurrentPriceRedisEntity> result4 = currentPriceRedisRepository.fetchPriceBy("  ");
		// then
		Assertions.assertThat(result1).isEmpty();
		Assertions.assertThat(result2).isEmpty();
		Assertions.assertThat(result3).isEmpty();
		Assertions.assertThat(result4).isEmpty();
	}

	@DisplayName("fetchPriceBy - 존재하지 않는 티커 심볼로 조회 시 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenTickerSymbolDoesNotExist_thenReturnEmptyOptional() {
		// given
		String tickerSymbol = "000000";
		// when
		Optional<CurrentPriceRedisEntity> result = currentPriceRedisRepository.fetchPriceBy(tickerSymbol);
		// then
		Assertions.assertThat(result).isEmpty();
	}

	@DisplayName("clear - 저장된 모든 현재가 삭제")
	@Test
	void clear() {
		// given
		currentPriceRedisRepository.savePrice("005930", 50000L);
		currentPriceRedisRepository.savePrice("000660", 80000L);
		Assertions.assertThat(stringRedisTemplate.keys("cp:*")).hasSize(2);
		// when
		currentPriceRedisRepository.clear();
		// then
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy("005930")).isEmpty();
		Assertions.assertThat(currentPriceRedisRepository.fetchPriceBy("000660")).isEmpty();
		Assertions.assertThat(stringRedisTemplate.keys("cp:*")).isEmpty();
	}
}
