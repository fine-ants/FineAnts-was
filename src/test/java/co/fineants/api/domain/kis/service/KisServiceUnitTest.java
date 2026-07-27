package co.fineants.api.domain.kis.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.notification.event.publisher.PortfolioPublisher;
import co.fineants.api.domain.stock_target_price.event.publisher.StockTargetPricePublisher;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.application.StockCsvLineParser;
import co.fineants.stock.application.StockCsvParser;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
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
	private LocalDateTimeService spyLocalDateTimeService;

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
			spyLocalDateTimeService
		);
		StockCsvLineParser stockCsvLineParser = new StockCsvLineParser("TS");
		stockCsvParser = new StockCsvParser("\\$", stockCsvLineParser);

		given(delayManager.timeout()).willReturn(Duration.ofSeconds(1));
		given(delayManager.delay()).willReturn(Duration.ZERO);
		given(delayManager.fixedDelay()).willReturn(Duration.ZERO);
	}

	@DisplayName("객체 생성")
	@Test
	void canCreated() {
		assertThat(kisService).isNotNull();
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

	// @DisplayName("한국투자증권에 종목 현재가 요청중에 액세스 토큰이 만료되어 실패하게 되면, 해당 요청은 조회하지 않는다")
	// @Test
	// void refreshStockCurrentPrice_whenAccessTokenExpired_thenCancelStockCurrentPriceRequest() {
	// 	// given
	// 	List<String> tickers = readStocks(100).stream()
	// 		.map(Stock::getTickerSymbol)
	// 		.toList();
	// 	tickers.forEach(ticker -> given(mockedKisClient.fetchCurrentPrice(ticker))
	// 		.willReturn(Mono.error(KisApiRequestException.expiredAccessToken())));
	// 	// when
	// 	List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
	// 	// then
	// 	Assertions.assertThat(prices).isEmpty();
	// }
	//
	// @DisplayName("한국투자증권에 종목 현재가 요청중에 요청 건수 초과 에러시 재시도 또한 전부 실패하게 되면 리스트에 추가되지 않는다")
	// @Test
	// void refreshStockCurrentPrice_whenFailRetry_thenNotAddResultList() {
	// 	// given
	// 	List<String> tickers = readStocks(100).stream()
	// 		.map(Stock::getTickerSymbol)
	// 		.toList();
	// 	tickers.forEach(ticker -> given(mockedKisClient.fetchCurrentPrice(ticker))
	// 		.willReturn(Mono.error(KisApiRequestException.requestLimitExceeded())));
	// 	given(spyDelayManager.fixedDelay()).willReturn(Duration.ZERO);
	// 	// when
	// 	List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickers);
	// 	// then
	// 	Assertions.assertThat(prices).isEmpty();
	// }
	//
	// @DisplayName("종목 현재가 갱신시 예외가 발생하면 다시 시도하여 가격을 조회한다")
	// @Test
	// void refreshStockCurrentPrice_whenFailFetch_thenRetryFetch() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
	// 	List<Stock> stocks = stockRepository.saveAll(List.of(
	// 		createSamsungStock()
	// 	));
	// 	stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));
	//
	// 	given(mockedKisClient.fetchCurrentPrice("005930"))
	// 		.willReturn(Mono.error(KisApiRequestException.requestLimitExceeded()))
	// 		.willReturn(Mono.just(KisCurrentPrice.create("005930", 50000L)));
	// 	given(spyDelayManager.delay()).willReturn(Duration.ZERO);
	// 	given(spyDelayManager.fixedDelay()).willReturn(Duration.ZERO);
	//
	// 	List<String> tickerSymbols = stocks.stream()
	// 		.map(Stock::getTickerSymbol)
	// 		.toList();
	// 	// when
	// 	List<KisCurrentPrice> prices = kisService.refreshStockCurrentPrice(tickerSymbols);
	//
	// 	// then
	// 	assertThat(prices).hasSize(1);
	// 	CurrentPriceRedisEntity actual = currentPriceRepository.fetchPriceBy("005930").orElseThrow();
	// 	assertThat(actual)
	// 		.hasFieldOrPropertyWithValue("tickerSymbol", "005930")
	// 		.hasFieldOrPropertyWithValue("price", 50000L);
	// }
	//
	// @DisplayName("종가 갱신시 요청건수 초과로 실패하였다가 다시 시도하여 성공한다")
	// @Test
	// void refreshLastDayClosingPriceWhenExceedingTransactionPerSecond() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
	// 	List<Stock> stocks = stockRepository.saveAll(List.of(
	// 		createSamsungStock()
	// 	));
	// 	stocks.forEach(stock -> portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock)));
	//
	// 	kisAccessTokenInMemoryRepository.save(createKisAccessToken());
	// 	given(mockedKisClient.fetchClosingPrice(anyString()))
	// 		.willThrow(KisApiRequestException.requestLimitExceeded())
	// 		.willThrow(KisApiRequestException.requestLimitExceeded())
	// 		.willReturn(Mono.just(KisClosingPrice.create("005930", 10000L)));
	// 	given(spyDelayManager.fixedDelay()).willReturn(Duration.ZERO);
	// 	List<String> tickerSymbols = stocks.stream()
	// 		.map(Stock::getTickerSymbol)
	// 		.toList();
	// 	// when
	// 	kisService.refreshClosingPrice(tickerSymbols);
	//
	// 	// then
	// 	verify(mockedKisClient, times(3)).fetchClosingPrice(anyString());
	// }
	//
	// @DisplayName("한국투자증권에 상장된 종목 정보를 조회한다")
	// @Test
	// void fetchStockInfoInRangedIpo() {
	// 	// given
	// 	KisAccessToken kisAccessToken = createKisAccessToken();
	// 	kisAccessTokenInMemoryRepository.save(kisAccessToken);
	//
	// 	KisIpoResponse kisIpoResponse = KisIpoResponse.create(
	// 		List.of(KisIpo.create("20240326", "000660", "에스케이하이닉스보통주"))
	// 	);
	// 	given(mockedKisClient.fetchIpo(
	// 		any(LocalDate.class),
	// 		any(LocalDate.class)
	// 	))
	// 		.willReturn(Mono.just(kisIpoResponse));
	//
	// 	KisSearchStockInfo kisSearchStockInfo = KisSearchStockInfo.listedStock("KR7000660001", "000660", "에스케이하이닉스보통주",
	// 		"SK hynix", "STK", "시가총액규모대", "전기,전자", "전기,전자");
	// 	given(mockedKisClient.fetchSearchStockInfo(anyString()))
	// 		.willReturn(Mono.just(kisSearchStockInfo));
	// 	// when
	// 	Flux<StockIntegrationInfo> stocks = kisService.fetchStockInfoInRangedIpo();
	// 	// then
	// 	StepVerifier.create(stocks)
	// 		.expectNext(
	// 			StockIntegrationInfo.create("000660", "에스케이하이닉스보통주", "SK hynix", "KR7000660001",
	// 				"전기,전자", Market.KOSPI))
	// 		.expectComplete()
	// 		.verify();
	// }
	//
	// @DisplayName("상장된 종목들의 상세 종목을 조회할 때 별도의 스레드에서 blocking되면 안된다")
	// @Test
	// void fetchStockInfoInRangedIpo_shouldNotBlockInSeparateThread() {
	// 	// given
	// 	given(mockedKisClient.fetchIpo(
	// 		any(LocalDate.class),
	// 		any(LocalDate.class)
	// 	)).willReturn(Mono.error(() -> new IllegalStateException(
	// 		"blockOptional() is blocking, which is not supported in thread parallel-1")));
	// 	// when
	// 	Flux<StockIntegrationInfo> result = kisService.fetchStockInfoInRangedIpo();
	// 	// then
	// 	StepVerifier.create(result)
	// 		.expectNextCount(0)
	// 		.expectComplete()
	// 		.verify();
	// }
	//
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
