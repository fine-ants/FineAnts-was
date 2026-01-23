package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.fineants.AbstractContainerBaseTest;

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
}
