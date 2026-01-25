package co.fineants.api.domain.kis.service;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.PriceRepository;
import reactor.core.publisher.Mono;

class CurrentPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceService service;

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private KisClient mockedKisClient;

	@DisplayName("특정 종목의 현재가가 없으면 빈 Optional을 반환한다.")
	@Test
	void fetchPrice_whenCurrentPriceIsAbsent_thenReturnEmptyOptional() {
		Optional<Money> price = service.fetchPrice("005930");

		Assertions.assertThat(price).isEmpty();
	}

	@DisplayName("특정 종목의 현재가를 조회한다.")
	@Test
	void fetchPrice() {
		// given
		String tickerSymbol = "005930";
		long expectedPrice = 50000L;
		priceRepository.savePrice(tickerSymbol, expectedPrice);

		// when
		Optional<Money> actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).contains(Money.won(50000L));
	}

	@DisplayName("종목의 현재가가 캐시 저장소에 없으면 외부 API를 호출하여 가져온다.")
	@Test
	void fetchPrice_whenPriceIsNotInCache_thenFetchFromExternalApi() {
		// given
		String tickerSymbol = "000660";
		BDDMockito.given(mockedKisClient.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, 50000L)));

		// when
		Optional<Money> actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).contains(Money.won(50000L));
	}
}
