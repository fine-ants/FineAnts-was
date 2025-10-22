package co.fineants.stock.presentation;

import static org.hamcrest.Matchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.stock.domain.dto.request.StockSearchRequest;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.success.StockSuccessCode;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

class StockRestControllerTest extends AbstractContainerBaseTest {

	@LocalServerPort
	private int port;

	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	@DisplayName("주식 종목을 검색한다")
	@Test
	void search() {
		Stock stock = TestDataFactory.createSamsungStock();
		stockRepository.save(stock);

		String bodyJson = ObjectMapperUtil.serialize(new StockSearchRequest("삼성"));
		RestAssured.given()
			.contentType(ContentType.JSON)
			.body(bodyJson)
			.log().all()
			.when()
			.post("/api/stocks/search")
			.then()
			.log().all()
			.statusCode(HttpStatus.OK.value())
			.body("code", equalTo(HttpStatus.OK.value()))
			.body("status", equalTo(HttpStatus.OK.name()))
			.body("message", equalTo(StockSuccessCode.OK_SEARCH_STOCKS.getMessage()))
			.body("data.size()", is(1))
			.body("data[0].stockCode", equalTo(stock.getStockCode()))
			.body("data[0].tickerSymbol", equalTo(stock.getTickerSymbol()))
			.body("data[0].companyName", equalTo(stock.getCompanyName()))
			.body("data[0].companyNameEng", equalTo(stock.getCompanyNameEng()))
			.body("data[0].market", equalTo(stock.getMarket().name()));
	}

	@DisplayName("종목 스크롤 검색을 한다")
	@Test
	void search_whenHttpMethodIsGet() {
		Stock stock = TestDataFactory.createSamsungStock();
		stockRepository.save(stock);

		RestAssured.given()
			.log().all()
			.queryParam("keyword", "삼성")
			.when()
			.get("/api/stocks/search")
			.then()
			.log().all()
			.statusCode(HttpStatus.OK.value())
			.body("code", equalTo(HttpStatus.OK.value()))
			.body("status", equalTo(HttpStatus.OK.name()))
			.body("message", equalTo(StockSuccessCode.OK_SEARCH_STOCKS.getMessage()))
			.body("data.size()", is(1))
			.body("data[0].stockCode", equalTo(stock.getStockCode()))
			.body("data[0].tickerSymbol", equalTo(stock.getTickerSymbol()))
			.body("data[0].companyName", equalTo(stock.getCompanyName()))
			.body("data[0].companyNameEng", equalTo(stock.getCompanyNameEng()))
			.body("data[0].market", equalTo(stock.getMarket().name()));
	}

	@DisplayName("특정 주식 종목을 상세 조회한다")
	@Test
	void getStock() {
		Stock stock = TestDataFactory.createSamsungStock();
		StockDividend samsungStockDividend = TestDataFactory.createSamsungStockDividend();
		stock.addStockDividend(samsungStockDividend);
		stockRepository.save(stock);

		int currentPrice = 68000;
		priceRepository.savePrice(stock, currentPrice);
		int closingPrice = 56000;
		closingPriceRepository.addPrice(stock.getTickerSymbol(), closingPrice);

		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(samsungStockDividend.getDividendDates().getRecordDate().minusDays(1));

		RestAssured.given()
			.when()
			.get("/api/stocks/{tickerSymbol}", stock.getTickerSymbol())
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("code", equalTo(HttpStatus.OK.value()))
			.body("status", equalTo(HttpStatus.OK.name()))
			.body("message", equalTo(StockSuccessCode.OK_SEARCH_DETAIL_STOCK.getMessage()))
			.body("data.stockCode", equalTo(stock.getStockCode()))
			.body("data.tickerSymbol", equalTo(stock.getTickerSymbol()))
			.body("data.companyName", equalTo(stock.getCompanyName()))
			.body("data.companyNameEng", equalTo(stock.getCompanyNameEng()))
			.body("data.market", equalTo(stock.getMarket().name()))
			.body("data.currentPrice", equalTo(currentPrice))
			.body("data.dailyChange", equalTo(12000))
			.body("data.dailyChangeRate", equalTo(21.43F))
			.body("data.sector", equalTo(stock.getSector()))
			.body("data.annualDividend", equalTo(361))
			.body("data.annualDividendYield", equalTo(0.53F))
			.body("data.dividendMonths[0]", equalTo(5));
	}
}
