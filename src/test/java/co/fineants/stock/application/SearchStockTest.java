package co.fineants.stock.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.kis.client.KisAccessToken;
import co.fineants.api.domain.kis.client.KisClient;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.domain.dto.response.KisClosingPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.response.StockResponse;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import reactor.core.publisher.Mono;

class SearchStockTest extends AbstractContainerBaseTest {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private SearchStock searchStock;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private KisClient kisClient;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@BeforeEach
	void setUp() {
		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 1, 1));
	}

	@DisplayName("종목 이름을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@ParameterizedTest
	@MethodSource(value = "co.fineants.TestDataProvider#validSearchTermSource")
	void search_givenStockName_whenSearch_thenReturnStockSearchItemList(String searchTerm) {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		stockRepository.save(TestDataFactory.createNokwonCI());

		// when
		List<StockSearchItem> items = searchStock.search(searchTerm);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("키워드가 null이면 빈 리스트를 반환한다")
	@Test
	void search_whenSearchTermIsNull_ThenReturnEmptyList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		stockRepository.save(TestDataFactory.createNokwonCI());

		String searchTerm = null;
		// when
		List<StockSearchItem> items = searchStock.search(searchTerm);
		// then
		assertThat(items).isEmpty();
	}

	@DisplayName("사이즈, 키워드를 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenTickerSymbolAndSizeAndKeyword_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		String tickerSymbol = null;
		int size = 10;
		String keyword = "삼성";
		// when
		List<StockSearchItem> items = searchStock.search(tickerSymbol, size, keyword);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("티커 심볼과 사이즈, 키워드를 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenTickerSymbolAndSizeAndKeyword2_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		String tickerSymbol = "006000";
		int size = 10;
		String keyword = "삼성";
		// when
		List<StockSearchItem> items = searchStock.search(tickerSymbol, size, keyword);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("사용자는 종목 정보를 상세 조회합니다")
	@Test
	void findDetailedStock() {
		// given
		Stock samsung = TestDataFactory.createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(samsung::addStockDividend);
		Stock saveSamsung = stockRepository.save(samsung);

		currentPriceRepository.savePrice(KisCurrentPrice.create("005930", 50000L));
		closingPriceRepository.savePrice(KisClosingPrice.create("005930", 49000L));
		given(kisClient.fetchAccessToken())
			.willReturn(
				Mono.just(new KisAccessToken("accessToken", "Bearer", LocalDateTime.now().plusSeconds(86400), 86400)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = searchStock.findDetailedStock(tickerSymbol);
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
					Money.won(361),
					Percentage.from(0.0072)
				)
		);
	}

	@DisplayName("사용자가 종목 상세 정보 조회시 종목의 현재가 및 종가가 없는 경우 외부 API를 통해서 가져온다")
	@Test
	void findDetailedStock_whenCurrentPriceAndClosingPriceNotExist_thenFetchFromExternalApi() {
		// given
		Stock samsung = TestDataFactory.createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(samsung::addStockDividend);
		Stock saveSamsung = stockRepository.save(samsung);

		given(mockedKisService.fetchCurrentPrice(anyString()))
			.willReturn(Mono.just(KisCurrentPrice.create(saveSamsung.getTickerSymbol(), 50000L)));
		given(mockedKisService.fetchClosingPrice(anyString()))
			.willReturn(Mono.just(KisClosingPrice.create(saveSamsung.getTickerSymbol(), 49000L)));

		String tickerSymbol = "005930";
		// when
		StockResponse response = searchStock.findDetailedStock(tickerSymbol);
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
					Money.won(361),
					Percentage.from(0.0072)
				)
		);
	}
}
