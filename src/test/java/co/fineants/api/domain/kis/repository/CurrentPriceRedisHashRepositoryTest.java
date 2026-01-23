package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;

class CurrentPriceRedisHashRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceRedisHashRepository repository;

	@Autowired
	private StringRedisTemplate template;

	@DisplayName("savePrice - 티커 심볼로 현재가 저장")
	@Test
	void savePrice() {
		// given
		String tickerSymbol = "005930";
		long price = 50000L;

		// when
		repository.savePrice(tickerSymbol, price);

		// then
		Assertions.assertThat(template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, tickerSymbol))
			.isEqualTo(String.valueOf(price));
	}

	@DisplayName("savePrice - 티커 심볼이 유효하지 않을때 저장하지 않음")
	@Test
	void savePrice_whenTickerSymbolIsInvalid_thenDoNothing() {
		// given
		long price = 50000L;
		// when
		repository.savePrice((String)null, price);
		repository.savePrice("", price);
		repository.savePrice(" ", price);
		repository.savePrice("  ", price);
		// then
		Long size = template.opsForHash().size(CurrentPriceRedisHashRepository.KEY);
		Assertions.assertThat(size).isZero();
	}

	@DisplayName("savePrice - 가격이 음수일때 저장하지 않음")
	@Test
	void savePrice_whenPriceIsNegative_thenDoNothing() {
		// given
		String tickerSymbol = "005930";
		long price = -1L;
		// when
		repository.savePrice(tickerSymbol, price);
		// then
		Long size = template.opsForHash().size(CurrentPriceRedisHashRepository.KEY);
		Assertions.assertThat(size).isZero();
	}

	@DisplayName("savePrice - 티커 심볼로 현재가 저장")
	@Test
	void savePrice_whenKisCurrentPrice_thenSaveThePrice() {
		// given
		KisCurrentPrice kisCurrentPrice1 = KisCurrentPrice.create("005930", 50000L);
		KisCurrentPrice kisCurrentPrice2 = KisCurrentPrice.create("035720", 30000L);

		// when
		repository.savePrice(kisCurrentPrice1, kisCurrentPrice2);

		// then
		Assertions.assertThat(template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, "005930"))
			.isEqualTo(String.valueOf(50000L));
		Assertions.assertThat(template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, "035720"))
			.isEqualTo(String.valueOf(30000L));
	}

	@DisplayName("fetchPriceBy - 티커 심볼로 현재가 조회")
	@Test
	void fetchPriceBy() {
		// given
		String tickerSymbol = "005930";
		long price = 50000L;
		repository.savePrice(tickerSymbol, price);

		// when
		Optional<Money> currentPrice = repository.fetchPriceBy(tickerSymbol);

		// then
		Assertions.assertThat(currentPrice).isPresent();
		Assertions.assertThat(currentPrice.orElseThrow()).isEqualTo(Money.won(price));
	}

	@DisplayName("fetchPriceBy - 저장되지 않은 티커 심볼로 조회시 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenNotSave_thenReturnEmptyOptional() {
		// given
		String tickerSymbol = "005930";

		// when
		Optional<Money> currentPrice = repository.fetchPriceBy(tickerSymbol);

		// then
		Assertions.assertThat(currentPrice).isEmpty();
	}

	@DisplayName("fetchPriceBy - 티커 심볼이 유효하지 않을때 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenTickerSymbolIsInvalid_thenReturnEmptyOptional() {
		// when & then
		Assertions.assertThat(repository.fetchPriceBy((String)null)).isEmpty();
		Assertions.assertThat(repository.fetchPriceBy(Strings.EMPTY)).isEmpty();
		Assertions.assertThat(repository.fetchPriceBy("  ")).isEmpty();
	}
}
