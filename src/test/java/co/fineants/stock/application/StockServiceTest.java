package co.fineants.stock.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.request.StockSearchRequest;
import co.fineants.stock.presentation.dto.response.StockDataResponse;
import co.fineants.stock.presentation.dto.response.StockReloadResponse;
import co.fineants.stock.presentation.dto.response.StockResponse;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import jakarta.persistence.EntityManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class StockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private CurrentPriceRedisRepository currentPriceRedisRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private FetchStockService fetchStockService;

	@Autowired
	private KisClient kisClient;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private DelayManager spyDelayManager;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 1, 1));
	}

	@DisplayName("사용자는 종목 정보를 상세 조회합니다")
	@Test
	void getDetailedStock() {
		// given
		Stock samsung = createSamsungStock();
		createStockDividendWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);
		Stock saveSamsung = stockRepository.save(samsung);

		currentPriceRedisRepository.savePrice(KisCurrentPrice.create("005930", 50000L));
		closingPriceRepository.addPrice(KisClosingPrice.create("005930", 49000L));
		given(kisClient.fetchAccessToken())
			.willReturn(
				Mono.just(new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusSeconds(86400), 86400)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = stockService.getDetailedStock(tickerSymbol);
		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting(
					StockResponse::getStockCode,
					StockResponse::getTickerSymbol,
					StockResponse::getCompanyName,
					StockResponse::getCompanyNameEng,
					StockResponse::getMarket,
					StockResponse::getCurrentPrice,
					StockResponse::getDailyChange,
					StockResponse::getDailyChangeRate,
					StockResponse::getSector,
					StockResponse::getAnnualDividend,
					StockResponse::getAnnualDividendYield)
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					saveSamsung.getStockCode(),
					saveSamsung.getTickerSymbol(),
					saveSamsung.getCompanyName(),
					saveSamsung.getCompanyNameEng(),
					saveSamsung.getMarket(),
					Money.won(50000),
					Money.won(1000),
					Percentage.from(0.0204),
					saveSamsung.getSector(),
					Money.won(1083),
					Percentage.from(0.0217)
				)
		);
	}

	@DisplayName("사용자가 종목 상세 정보 조회시 종목의 현재가 및 종가가 없는 경우 서버로부터 조회하여 가져온다")
	@Test
	void getDetailedStock_whenPriceIsNotExist_thenFetchCurrentPrice() {
		// given
		Stock samsung = createSamsungStock();
		createStockDividendWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);
		Stock saveSamsung = stockRepository.save(samsung);

		given(kisClient.fetchAccessToken())
			.willReturn(
				Mono.just(new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusDays(1), 3600 * 24)));
		given(kisClient.fetchCurrentPrice(anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(saveSamsung.getTickerSymbol(), 50000L)));
		given(kisClient.fetchClosingPrice(anyString()))
			.willReturn(Mono.just(KisClosingPrice.create(saveSamsung.getTickerSymbol(), 49000L)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = stockService.getDetailedStock(tickerSymbol);
		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting(
					StockResponse::getStockCode,
					StockResponse::getTickerSymbol,
					StockResponse::getCompanyName,
					StockResponse::getCompanyNameEng,
					StockResponse::getMarket,
					StockResponse::getCurrentPrice,
					StockResponse::getDailyChange,
					StockResponse::getDailyChangeRate,
					StockResponse::getSector,
					StockResponse::getAnnualDividend,
					StockResponse::getAnnualDividendYield)
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					saveSamsung.getStockCode(),
					saveSamsung.getTickerSymbol(),
					saveSamsung.getCompanyName(),
					saveSamsung.getCompanyNameEng(),
					saveSamsung.getMarket(),
					Money.won(50000),
					Money.won(1000),
					Percentage.from(0.0204),
					saveSamsung.getSector(),
					Money.won(1083),
					Percentage.from(0.0217)
				)
		);
	}

	@Transactional
	@DisplayName("상장된 종목과 폐지된 종목을 조회하여 최신화한다")
	@Test
	void reloadStocks() {
		// given
		Stock nokwon = stockRepository.save(createNokwonCI());

		StockDataResponse.StockIntegrationInfo hynix = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		given(mockedKisService.fetchStockInfoInRangedIpo())
			.willReturn(Flux.just(hynix));
		given(mockedKisService.fetchSearchStockInfo(hynix.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
				"KR7000660001",
				"000660",
				"에스케이하이닉스보통주",
				"SK hynix",
				"STK",
				"시가총액규모대",
				"전기,전자",
				"전기,전자"))
			);
		given(mockedKisService.fetchSearchStockInfo(nokwon.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.delistedStock("KR7065560005", "065560", "녹원씨엔아이",
				"Nokwon Commercials & Industries, Inc.",
				"KSQ", "시가총액규모대", "소프트웨어", "소프트웨어", LocalDate.of(2024, 7, 29))));
		DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE;
		given(mockedKisService.fetchDividend(hynix.getTickerSymbol()))
			.willReturn(Flux.just(KisDividend.create(hynix.getTickerSymbol(),
					Money.won(300),
					LocalDate.parse("20240331", dtf),
					LocalDate.parse("20240514", dtf)),
				KisDividend.create(hynix.getTickerSymbol(),
					Money.won(300),
					LocalDate.parse("20240630", dtf),
					LocalDate.parse("20240814", dtf))));
		given(spyDelayManager.delay()).willReturn(Duration.ZERO);
		// when
		StockReloadResponse response = stockService.reloadStocks();
		// then
		entityManager.flush();
		entityManager.clear();

		assertThat(response).isNotNull();
		assertThat(response.getAddedStocks()).hasSize(1);
		assertThat(response.getDeletedStocks()).hasSize(1);

		Stock deletedStock = stockRepository.findByTickerSymbolIncludingDeleted(nokwon.getTickerSymbol()).orElseThrow();
		assertThat(deletedStock.isDeleted()).isTrue();

		Stock findHynix = stockRepository.findByTickerSymbol(hynix.getTickerSymbol()).orElseThrow();
		assertThat(findHynix.getStockDividends())
			.hasSize(2)
			.extracting(StockDividend::getDividend, StockDividend::getDividendDates)
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactly(
				Tuple.tuple(
					Money.won(300),
					DividendDates.of(
						LocalDate.parse("20240331", dtf),
						LocalDate.parse("20240329", dtf),
						LocalDate.parse("20240514", dtf)
					)
				),
				Tuple.tuple(
					Money.won(300),
					DividendDates.of(
						LocalDate.parse("20240630", dtf),
						LocalDate.parse("20240628", dtf),
						LocalDate.parse("20240814", dtf)
					)
				)
			);
	}

	@DisplayName("종목 최신화 수행중에 별도의 스레드에서 blocking되면 안된다")
	@Test
	void reloadStocks_shouldNotBlockingThread_whenFetchSearchStockInfo() {
		// given
		given(mockedKisService.fetchStockInfoInRangedIpo())
			.willReturn(Flux.error(
				new IllegalStateException("blockOptional() is blocking, which is not supported in thread parallel-1")));
		given(mockedKisService.fetchSearchStockInfo(anyString()))
			.willReturn(Mono.error(
				new IllegalStateException("blockOptional() is blocking, which is not supported in thread parallel-1")));
		given(mockedKisService.fetchDividend(anyString()))
			.willReturn(Flux.error(
				new IllegalStateException("blockOptional() is blocking, which is not supported in thread parallel-1")));
		// when
		StockReloadResponse response = stockService.reloadStocks();
		// then
		assertThat(response.getAddedStocks()).isEmpty();
		assertThat(response.getDeletedStocks()).isEmpty();
		assertThat(response.getAddedDividends()).isEmpty();
	}

	@DisplayName("종목 정보를 최신 정보로 갱신한다")
	@Test
	void givenStocks_whenSyncAllStocksWithLatestData_thenUpdateLatestData() {
		// given
		Stock samsung = stockRepository.save(createSamsungStock());
		createStockDividendWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);
		given(mockedKisService.fetchSearchStockInfo(samsung.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
				samsung.getStockCode(),
				samsung.getTickerSymbol(),
				samsung.getCompanyName(),
				samsung.getCompanyNameEng(),
				"KSQ",
				"시가총액규모대",
				"의료",
				"의료"
			)));
		// when
		List<Stock> actual = stockService.syncAllStocksWithLatestData();
		// then
		assertThat(actual).hasSize(1);
		assertThat(actual.get(0).getMarket()).isEqualTo(Market.KOSDAQ);
		assertThat(actual.get(0).getSector()).isEqualTo("의료");

		actual = fetchStockService.fetchStocks();
		assertThat(actual).hasSize(1);
	}

	@DisplayName("종목 이름을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenStockName_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(createSamsungStock());
		stockRepository.save(createNokwonCI());

		String searchTerm = "삼성";
		StockSearchRequest request = new StockSearchRequest(searchTerm);
		// when
		List<StockSearchItem> items = stockService.search(request);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("티커 심볼을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenTickerSymbol_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(createSamsungStock());
		stockRepository.save(createNokwonCI());

		String searchTerm = "005930";
		StockSearchRequest request = new StockSearchRequest(searchTerm);
		// when
		List<StockSearchItem> items = stockService.search(request);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("영어 종목 이름을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenStockNameEng_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(createSamsungStock());
		stockRepository.save(createNokwonCI());

		String searchTerm = "samsung";
		StockSearchRequest request = new StockSearchRequest(searchTerm);
		// when
		List<StockSearchItem> items = stockService.search(request);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("키워드가 null이면 빈 리스트를 반환한다")
	@Test
	void search_whenSearchTermIsNull_ThenReturnEmptyList() {
		// given
		stockRepository.save(createSamsungStock());
		stockRepository.save(createNokwonCI());

		String searchTerm = null;
		StockSearchRequest request = new StockSearchRequest(searchTerm);
		// when
		List<StockSearchItem> items = stockService.search(request);
		// then
		assertThat(items).isEmpty();
	}
}
