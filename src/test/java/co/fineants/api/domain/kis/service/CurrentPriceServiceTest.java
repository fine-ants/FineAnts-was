package co.fineants.api.domain.kis.service;

import java.time.Clock;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

	@Autowired
	private Clock spyClock;

	@Value("${stock.current-price.freshness-threshold-millis:300000}")
	private long freshnessThresholdMillis;

	@DisplayName("특정 종목의 현재가를 조회한다.")
	@Test
	void fetchPrice() {
		// given
		String tickerSymbol = "005930";
		long expectedPrice = 50000L;
		priceRepository.savePrice(tickerSymbol, expectedPrice);

		// when
		Money price = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(price).isEqualTo(Money.won(50000L));
	}

	@DisplayName("종목의 현재가가 캐시 저장소에 없으면 외부 API를 호출하여 가져온다.")
	@Test
	void fetchPrice_whenPriceIsNotInCache_thenFetchFromExternalApi() {
		// given
		String tickerSymbol = "000660";
		long price = 50000L;
		BDDMockito.given(mockedKisClient.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, price)));

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(price));
		Assertions.assertThat(priceRepository.getCachedPrice(tickerSymbol))
			.isPresent()
			.contains(Money.won(price));
	}

	@DisplayName("특정 종목의 현재가가 없고, 외부 API에서도 가져올 수 없으면 예외를 던진다.")
	@Test
	void fetchPrice_whenPriceNotFound_thenThrowException() {
		// given
		String tickerSymbol = "005930";
		BDDMockito.given(mockedKisClient.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.empty());
		// when
		Throwable throwable = Assertions.catchThrowable(() -> {
			service.fetchPrice(tickerSymbol);
		});
		// then
		BDDAssertions.then(throwable)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("현재가를 가져올 수 없습니다. tickerSymbol=" + tickerSymbol);
	}

	@DisplayName("특정 종목의 현재가가 존재하고, 신선도(freshness) 기준에 맞으면 캐시된 가격을 반환한다.")
	@Test
	void fetchPrice_whenPriceIsFresh_thenReturnCachedPrice() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis - 1L);
		String tickerSymbol = "005930";
		long freshPrice = 50000L;
		priceRepository.savePrice(tickerSymbol, freshPrice);

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(freshPrice));
		Assertions.assertThat(priceRepository.getCachedPrice(tickerSymbol))
			.isPresent()
			.contains(Money.won(freshPrice));
	}

	@DisplayName("특정 종목의 현재가가 존재하지만 신선도(freshness) 기준에 맞지 않으면 외부 API를 호출하여 최신 가격을 가져온다.")
	@Test
	void fetchPrice_whenPriceIsStale_thenFetchFromExternalApi() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		String tickerSymbol = "005930";
		long stalePrice = 45000L;
		priceRepository.savePrice(tickerSymbol, stalePrice);

		long freshPrice = 50000L;
		BDDMockito.given(mockedKisClient.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, freshPrice)));

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(freshPrice));
		Assertions.assertThat(priceRepository.getCachedPrice(tickerSymbol))
			.isPresent()
			.contains(Money.won(freshPrice));
	}
}
