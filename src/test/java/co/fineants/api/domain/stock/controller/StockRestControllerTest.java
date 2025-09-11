package co.fineants.api.domain.stock.controller;

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
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.success.StockSuccessCode;
import io.restassured.RestAssured;

class StockRestControllerTest extends AbstractContainerBaseTest {

	@LocalServerPort
	private int port;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

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

	@DisplayName("주식 종목을 조회한다.")
	@Test
	void getStock() {
		Stock stock = TestDataFactory.createSamsungStock();
		stockRepository.save(stock);

		StockDividend samsungStockDividend = TestDataFactory.createSamsungStockDividend(stock);
		stockDividendRepository.save(samsungStockDividend);

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
