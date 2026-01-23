package co.fineants.api.domain.kis.repository;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;

class CurrentPriceRedisHashRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceRedisHashRepository repository;

	@Autowired
	private StringRedisTemplate template;

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
}
