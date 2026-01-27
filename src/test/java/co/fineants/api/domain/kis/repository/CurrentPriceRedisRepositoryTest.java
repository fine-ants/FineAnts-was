package co.fineants.api.domain.kis.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import reactor.core.publisher.Mono;

class CurrentPriceRedisRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private KisClient mockedKisClient;

	@DisplayName("fetchPriceBy - 저장된 현재가가 있을때 저장된 현재가를 반환")
	@Test
	void fetchPriceBy_whenNotStorePrice_thenFetchPriceFromKis() {
		// given
		String ticker = "005930";
		BDDMockito.given(mockedKisClient.fetchCurrentPrice(ticker))
			.willReturn(Mono.just(KisCurrentPrice.create(ticker, 50000L)));
		// when
		CurrentPriceRedisEntity entity = currentPriceRedisRepository.fetchPriceBy(ticker).orElseThrow();
		// then
		Assertions.assertThat(entity)
			.hasFieldOrPropertyWithValue("tickerSymbol", ticker)
			.hasFieldOrPropertyWithValue("price", 50000L);
	}
}
