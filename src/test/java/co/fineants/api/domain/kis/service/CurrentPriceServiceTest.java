package co.fineants.api.domain.kis.service;

import java.time.Clock;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.CurrentPriceRedisEntity;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import reactor.core.publisher.Mono;

class CurrentPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private CurrentPriceService service;

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private KisService kisService;

	@Autowired
	private Clock spyClock;

	@Value("${stock.current-price.freshness-threshold-millis:5000}")
	private long freshnessThresholdMillis;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private StockRepository stockRepository;

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

	@DisplayName("종목의 현재가가 캐시 저장소에 없으면 종목 현재가 갱신 동기식 이벤트를 발행하고, 종가 데이터를 반환한다")
	@Test
	void fetchPrice_whenPriceNotInCache_thenPublishStockCurrentPriceRefreshSyncEventAndReturnClosingPrice() {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		String tickerSymbol = stock.getTickerSymbol();
		long freshPrice = 50000L;
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, freshPrice)));
		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(freshPrice));
		CurrentPriceRedisEntity actual = priceRepository.fetchPriceBy(tickerSymbol).orElseThrow();
		Assertions.assertThat(actual)
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", freshPrice);
	}

	@DisplayName("캐시 저장소에 종목 현재가가 있지만 신선도를 만족하지 않아서 이벤트를 발행하고, 기존 현재가 데이터를 반환한다.")
	@Test
	void fetchPrice_whenCurrentPriceIsStale_thenPublishStockCurrentPriceRefreshEventAndReturnStaleCurrentPrice() {
		// given
		BDDMockito.given(spyClock.millis())
			.willReturn(1_000_000L)  // initial time
			.willReturn(1_000_000L + freshnessThresholdMillis + 1L);
		String tickerSymbol = "005930";
		long stalePrice = 45000L;
		priceRepository.savePrice(tickerSymbol, stalePrice);
		long freshPrice = 50000L;
		BDDMockito.given(kisService.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(KisCurrentPrice.create(tickerSymbol, freshPrice)));

		// when
		Money actualPrice = service.fetchPrice(tickerSymbol);

		// then
		Assertions.assertThat(actualPrice).isEqualTo(Money.won(stalePrice));
		// then : 비동기 캐시 업데이트 검증 (최대 2초 대기)
		Awaitility.await()
			.atMost(Duration.ofSeconds(2))
			.untilAsserted(() ->
				Assertions.assertThat(priceRepository.fetchPriceBy(tickerSymbol).orElseThrow())
					.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
					.hasFieldOrPropertyWithValue("price", freshPrice));
	}

	@DisplayName("캐시 저장소의 종목 현재가가 존재하고, 신선도(freshness) 기준에 맞으면 캐시된 가격을 반환한다.")
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
		Assertions.assertThat(priceRepository.fetchPriceBy(tickerSymbol).orElseThrow())
			.hasFieldOrPropertyWithValue("tickerSymbol", tickerSymbol)
			.hasFieldOrPropertyWithValue("price", freshPrice);
	}
}
