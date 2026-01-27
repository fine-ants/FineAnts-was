package co.fineants.api.domain.kis.repository;

import java.time.Clock;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.global.util.ObjectMapperUtil;

class CurrentPriceRedisHashRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceRedisHashRepository repository;

	@Autowired
	private StringRedisTemplate template;

	@Autowired
	private Clock spyClock;

	@Value("${stock.current-price.freshness-threshold-millis:300000}")
	private long freshnessThresholdMillis;

	@DisplayName("savePrice - 티커 심볼로 현재가 저장")
	@Test
	void savePrice() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L);
		String tickerSymbol = "005930";
		long price = 50000L;

		// when
		repository.savePrice(tickerSymbol, price);

		// then
		CurrentPriceRedisEntity expected = CurrentPriceRedisEntity.of(tickerSymbol, price, spyClock.millis());
		String json = (String)template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, tickerSymbol);
		CurrentPriceRedisEntity actual = ObjectMapperUtil.deserialize(json, CurrentPriceRedisEntity.class);
		Assertions.assertThat(actual).isEqualTo(expected);
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
		String json = (String)template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, "005930");
		CurrentPriceRedisEntity actual = ObjectMapperUtil.deserialize(json, CurrentPriceRedisEntity.class);
		Assertions.assertThat(actual.getPrice()).isEqualTo(50000L);

		json = (String)template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, "035720");
		actual = ObjectMapperUtil.deserialize(json, CurrentPriceRedisEntity.class);
		Assertions.assertThat(actual.getPrice()).isEqualTo(30000L);
	}

	@DisplayName("savePrice - 티커 심볼로 현재가 저장")
	@Test
	void savePrice_whenKisCurrentPriceIsNull_thenDoNothing() {
		// when
		repository.savePrice((KisCurrentPrice[])null);
		repository.savePrice();

		// then
		Long size = template.opsForHash().size(CurrentPriceRedisHashRepository.KEY);
		Assertions.assertThat(size).isZero();
	}

	@DisplayName("savePrice - 동일한 tickerSymbol로 여러번 저장시 마지막 값으로 저장")
	@Test
	void savePrice_whenSaveMultipleTimes_thenLastValueIsSaved() {
		// given
		KisCurrentPrice[] kisCurrentPrices = new KisCurrentPrice[2];
		String tickerSymbol = "005930";
		long firstPrice = 50000L;
		long secondPrice = 60000L;
		kisCurrentPrices[0] = KisCurrentPrice.create("005930", firstPrice);
		kisCurrentPrices[1] = KisCurrentPrice.create("005930", secondPrice);

		// when
		repository.savePrice(kisCurrentPrices);

		// then
		String json = (String)template.opsForHash().get(CurrentPriceRedisHashRepository.KEY, tickerSymbol);
		CurrentPriceRedisEntity actual = ObjectMapperUtil.deserialize(json, CurrentPriceRedisEntity.class);
		Assertions.assertThat(actual.getPrice()).isEqualTo(secondPrice);
	}

	@DisplayName("fetchPriceBy - 티커 심볼로 현재가 조회")
	@Test
	void fetchPriceBy() {
		// given
		String tickerSymbol = "005930";
		long price = 50000L;
		repository.savePrice(tickerSymbol, price);

		// when
		Optional<CurrentPriceRedisEntity> entity = repository.fetchPriceBy(tickerSymbol);

		// then
		Assertions.assertThat(entity).isPresent();
		Assertions.assertThat(entity.orElseThrow())
			.isEqualTo(CurrentPriceRedisEntity.of(tickerSymbol, price, spyClock.millis()));
	}

	@DisplayName("fetchPriceBy - 저장되지 않은 티커 심볼로 조회시 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenNotSave_thenReturnEmptyOptional() {
		// given
		String tickerSymbol = "005930";

		// when
		Optional<CurrentPriceRedisEntity> entity = repository.fetchPriceBy(tickerSymbol);

		// then
		Assertions.assertThat(entity).isEmpty();
	}

	@DisplayName("fetchPriceBy - 티커 심볼이 유효하지 않을때 빈 Optional 반환")
	@Test
	void fetchPriceBy_whenTickerSymbolIsInvalid_thenReturnEmptyOptional() {
		// when & then
		Assertions.assertThat(repository.fetchPriceBy(null)).isEmpty();
		Assertions.assertThat(repository.fetchPriceBy(Strings.EMPTY)).isEmpty();
		Assertions.assertThat(repository.fetchPriceBy("  ")).isEmpty();
	}
}
