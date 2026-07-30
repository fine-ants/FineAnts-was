package co.fineants.api.domain.kis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisIpo;
import co.fineants.api.domain.kis.domain.dto.response.KisIpoResponse;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.notification.event.publisher.PortfolioPublisher;
import co.fineants.api.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.KisApiRequestException;
import co.fineants.stock.application.StockCsvLineParser;
import co.fineants.stock.application.StockCsvParser;
import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.response.StockDataResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class KisServiceUnitTest {

	private KisService kisService;

	@Mock
	private StockRepository stockRepository;

	@Mock
	private KisAccessTokenService kisAccessTokenService;

	@Mock
	private CurrentPriceService currentPriceService;

	@Mock
	private ClosingPriceService closingPriceService;

	@Mock
	private StockTargetPricePublisher stockTargetPricePublisher;

	@Mock
	private PortfolioPublisher portfolioPublisher;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Mock
	private KisClient kisClient;

	@Mock
	private DelayManager delayManager;

	private StockCsvParser stockCsvParser;

	@BeforeEach
	void setUp() {
		kisService = new KisService(
			kisClient,
			currentPriceService,
			closingPriceService,
			stockTargetPricePublisher,
			portfolioPublisher,
			delayManager,
			kisAccessTokenService,
			stockRepository,
			localDateTimeService
		);
		StockCsvLineParser stockCsvLineParser = new StockCsvLineParser("TS");
		stockCsvParser = new StockCsvParser("\\$", stockCsvLineParser);

		BDDMockito.lenient().when(delayManager.timeout()).thenReturn(Duration.ofSeconds(1));
		BDDMockito.lenient().when(delayManager.fixedDelay()).thenReturn(Duration.ZERO);
	}

	@DisplayName("종목 현재가 조회")
	@Test
	void should_return_current_price() {
		// given
		String tickerSymbol = "005930";
		KisCurrentPrice kisCurrentPrice = KisCurrentPrice.create(tickerSymbol, 60000L);
		given(kisClient.fetchCurrentPrice(tickerSymbol))
			.willReturn(Mono.just(kisCurrentPrice));
		// when
		Mono<KisCurrentPrice> currentPrice = kisService.fetchCurrentPrice(tickerSymbol);
		// then
		StepVerifier.create(currentPrice)
			.expectNext(kisCurrentPrice)
			.verifyComplete();
	}

	@DisplayName("100개의 종목들의 현재가를 갱신한 다음에 저장소에 저장해야 한다")
	@Test
	void should_return_current_price_when_ticker_are_multiple_then_save_current_price() {
		// given
		List<String> tickers = readStocks(100).stream()
			.map(Stock::getTickerSymbol)
			.toList();
		tickers.forEach(ticker -> given(kisClient.fetchCurrentPrice(ticker))
			.willReturn(Mono.just(KisCurrentPrice.create(ticker, 50000L))));
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
		// then
		KisCurrentPrice[] expected = tickers.stream()
			.map(t -> KisCurrentPrice.create(t, 50_000L))
			.toArray(KisCurrentPrice[]::new);
		assertThat(prices)
			.hasSize(tickers.size())
			.containsExactly(expected);
		BDDMockito.verify(currentPriceService, times(100))
			.savePrice(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
		BDDMockito.verify(stockTargetPricePublisher, times(1))
			.publishEvent(tickers);
		BDDMockito.verify(portfolioPublisher, times(1))
			.publishCurrentPriceEvent();
	}

	private List<Stock> readStocks(int limit) {
		try {
			InputStream inputStream = new ClassPathResource("stocks.csv").getInputStream();
			return stockCsvParser.parse(inputStream).stream()
				.limit(limit)
				.toList();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@DisplayName("한국투자증권에 종목 현재가 요청중에 액세스 토큰이 만료되어 실패하게 되면, 해당 요청은 조회하지 않는다")
	@Test
	void should_not_fetch_current_price_when_access_token_is_expired() {
		// given
		String ticker = TestDataFactory.createSamsungStock().getTickerSymbol();
		List<String> tickers = List.of(ticker);
		BDDMockito.given(kisClient.fetchCurrentPrice(ticker))
			.willReturn(Mono.error(KisApiRequestException.expiredAccessToken()));
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
		// then
		Assertions.assertThat(prices).isEmpty();
		BDDMockito.verify(currentPriceService, times(0))
			.savePrice(anyString(), anyLong());
		BDDMockito.verify(stockTargetPricePublisher, times(0))
			.publishEvent(tickers);
		BDDMockito.verify(portfolioPublisher, times(0))
			.publishCurrentPriceEvent();
	}

	@DisplayName("한국투자증권에 종목 현재가 요청중에 요청 건수 초과 에러시 재시도 또한 전부 실패하게 되면 빈 리스트를 반환한다")
	@Test
	void should_return_empty_list_when_exceed_request_count_and_fail_to_retry_then_not_refresh_stock_current_price() {
		// given
		List<String> tickers = readStocks(200).stream()
			.map(Stock::getTickerSymbol)
			.toList();
		BDDMockito.given(kisClient.fetchCurrentPrice(argThat(tickers::contains)))
			.willReturn(Mono.error(KisApiRequestException.requestLimitExceeded()));
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
		// then
		Assertions.assertThat(prices).isEmpty();
		int maxAttempts = 5;
		int expectedTotalCalls = tickers.size() * (1 + maxAttempts);
		BDDMockito.verify(kisClient, BDDMockito.times(expectedTotalCalls))
			.fetchCurrentPrice(argThat(tickers::contains));
	}

	@DisplayName("종목 현재가 갱신시 예외가 발생하면 다시 시도하여 가격을 조회한다")
	@Test
	void should_return_refreshed_current_prices_when_response_error_response_from_kis_api_then_retry_request() {
		// given
		Stock stock = TestDataFactory.createSamsungStock();
		given(kisClient.fetchCurrentPrice(stock.getTickerSymbol()))
			.willReturn(Mono.error(KisApiRequestException.requestLimitExceeded()))
			.willReturn(Mono.just(KisCurrentPrice.create(stock.getTickerSymbol(), 50_000L)));

		List<String> tickerSymbols = List.of(stock.getTickerSymbol());
		// when
		List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickerSymbols);

		// then
		assertThat(prices).hasSize(1);
		BDDMockito.verify(currentPriceService, times(1))
			.savePrice(stock.getTickerSymbol(), 50_000L);
	}

	@DisplayName("종가 갱신시 요청건수 초과로 실패하였다가 다시 시도하여 성공한다")
	@Test
	void should_return_and_refresh_closing_price_when_response_error_response_from_kis_then_retry_request() {
		// given
		Stock stock = TestDataFactory.createSamsungStock();
		given(kisClient.fetchClosingPrice(stock.getTickerSymbol()))
			.willThrow(KisApiRequestException.requestLimitExceeded())
			.willThrow(KisApiRequestException.requestLimitExceeded())
			.willReturn(Mono.just(KisClosingPrice.create(stock.getTickerSymbol(), 10_000L)));
		List<String> tickerSymbols = List.of(stock.getTickerSymbol());
		// when
		List<KisClosingPrice> closingPrices = kisService.refreshClosingPrice(tickerSymbols);

		// then
		Assertions.assertThat(closingPrices)
			.hasSize(1);
		verify(kisClient, times(3))
			.fetchClosingPrice(stock.getTickerSymbol());
		verify(closingPriceService, times(1))
			.savePrice(stock.getTickerSymbol(), 10_000L);
	}

	@DisplayName("한국투자증권에 상장된 종목 정보를 조회한다")
	@Test
	void should_return_ipo_stocks_when_fetch_stock_info_in_ranged_ipo() {
		// given
		KisIpoResponse kisIpoResponse = KisIpoResponse.create(
			List.of(KisIpo.create("20240326", "000660", "에스케이하이닉스보통주"))
		);
		LocalDate baseTime = LocalDate.of(2026, 7, 30);
		BDDMockito.given(localDateTimeService.getLocalDateWithNow())
			.willReturn(baseTime);
		given(kisClient.fetchIpo(
			baseTime.minusDays(1),
			baseTime
		)).willReturn(Mono.just(kisIpoResponse));

		KisSearchStockInfo kisSearchStockInfo = KisSearchStockInfo.listedStock("KR7000660001", "000660", "에스케이하이닉스보통주",
			"SK hynix", "STK", "시가총액규모대", "전기,전자", "전기,전자");

		List<String> tickers = kisIpoResponse.getKisIpos().stream()
			.map(KisIpo::getShtCd)
			.toList();
		given(kisClient.fetchSearchStockInfo(argThat(tickers::contains)))
			.willReturn(Mono.just(kisSearchStockInfo));
		// when
		Flux<StockDataResponse.StockIntegrationInfo> stocks = kisService.fetchStockInfoInRangedIpo();
		// then
		StepVerifier.create(stocks)
			.expectNext(
				StockDataResponse.StockIntegrationInfo.create("000660", "에스케이하이닉스보통주", "SK hynix", "KR7000660001",
					"전기,전자", Market.KOSPI))
			.expectComplete()
			.verify();
	}

	@DisplayName("상장 종목 상세 정보 조회시 별도의 스레드에서 블로킹되면 안된다")
	@Test
	void should_not_blocking_thread_when_fetch_ipo() {
		// given
		LocalDate baseTime = LocalDate.of(2026, 7, 30);
		BDDMockito.given(localDateTimeService.getLocalDateWithNow())
			.willReturn(baseTime);
		given(kisClient.fetchIpo(
			baseTime.minusDays(1),
			baseTime
		)).willReturn(Mono.error(() -> new IllegalStateException(
			"blockOptional() is blocking, which is not supported in thread parallel-1")));
		// when
		Flux<StockDataResponse.StockIntegrationInfo> result = kisService.fetchStockInfoInRangedIpo();
		// then
		StepVerifier.create(result)
			.expectNextCount(0)
			.expectComplete()
			.verify();
	}

	// @DisplayName("사용자는 db에 저장된 종목을 각각 조회한다")
	// @Test
	// void fetchSearchStockInfo() {
	// 	// given
	// 	List<Stock> stocks = readStocks().stream()
	// 		.limit(100)
	// 		.toList();
	// 	List<String> tickerSymbols = stocks.stream()
	// 		.map(Stock::getTickerSymbol)
	// 		.toList();
	//
	// 	KisAccessToken kisAccessToken = createKisAccessToken();
	// 	kisAccessTokenInMemoryRepository.save(kisAccessToken);
	// 	stocks.forEach(s ->
	// 		given(mockedKisClient.fetchSearchStockInfo(s.getTickerSymbol()))
	// 			.willReturn(Mono.just(
	// 					KisSearchStockInfo.listedStock(
	// 						s.getStockCode(),
	// 						s.getTickerSymbol(),
	// 						s.getCompanyName(),
	// 						s.getCompanyNameEng(),
	// 						"STK",
	// 						"시가총액규모대",
	// 						s.getSector(),
	// 						s.getSector()
	// 					)
	// 				)
	// 			)
	// 	);
	//
	// 	// when & then
	// 	tickerSymbols.stream()
	// 		.map(kisService::fetchSearchStockInfo)
	// 		.map(Mono::just)
	// 		.forEach(mono ->
	// 			StepVerifier.create(mono)
	// 				.expectNextMatches(stockInfo -> {
	// 					Assertions.assertThat(stockInfo).isNotNull();
	// 					return true;
	// 				})
	// 				.verifyComplete()
	// 		);
	// }
	//
	// private List<Stock> readStocks() {
	// 	return readStocks(0);
	// }
	//
	// @DisplayName("사용자는 삼성전자의 올해 배당일정을 조회한다")
	// @Test
	// void fetchDividend() {
	// 	// given
	// 	String tickerSymbol = "005930";
	// 	KisAccessToken kisAccessToken = createKisAccessToken();
	// 	kisAccessTokenInMemoryRepository.save(kisAccessToken);
	// 	given(mockedKisClient.fetchDividendThisYear(tickerSymbol))
	// 		.willReturn(Mono.just(KisDividendWrapper.create(List.of(
	// 			KisDividend.create(tickerSymbol, Money.won(300), LocalDate.of(2024, 3, 1),
	// 				LocalDate.of(2024, 5, 1))))));
	// 	// when
	// 	Flux<KisDividend> dividends = kisService.fetchDividend(tickerSymbol);
	// 	// then
	// 	StepVerifier.create(dividends)
	// 		.expectNext(
	// 			KisDividend.create("005930", Money.won(300), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 5, 1)))
	// 		.expectComplete()
	// 		.verify();
	// }
	//
	// @DisplayName("사용자는 새로운 한국투자증권의 액세스 토큰을 발급받아서 배당 일정을 조회한다")
	// @Test
	// void fetchDividend_whenAccessTokenExpired_thenIssueAccessToken() {
	// 	// given
	// 	String tickerSymbol = "005930";
	// 	kisAccessTokenInMemoryRepository.save(null);
	// 	KisAccessToken newKisAccessToken = createKisAccessToken();
	// 	given(mockedKisClient.fetchAccessToken())
	// 		.willReturn(Mono.just(newKisAccessToken));
	// 	given(mockedKisClient.fetchDividendThisYear(tickerSymbol))
	// 		.willReturn(Mono.just(KisDividendWrapper.create(List.of(
	// 			KisDividend.create(tickerSymbol, Money.won(300), LocalDate.of(2024, 3, 1),
	// 				LocalDate.of(2024, 5, 1))))));
	// 	// when
	// 	Flux<KisDividend> dividends = kisService.fetchDividend(tickerSymbol);
	// 	// then
	// 	StepVerifier.create(dividends)
	// 		.expectNext(
	// 			KisDividend.create("005930", Money.won(300), LocalDate.of(2024, 3, 1), LocalDate.of(2024, 5, 1)))
	// 		.expectComplete()
	// 		.verify();
	// }
}
