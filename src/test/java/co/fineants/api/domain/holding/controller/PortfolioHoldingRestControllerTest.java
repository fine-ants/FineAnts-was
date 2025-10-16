package co.fineants.api.domain.holding.controller;

import static co.fineants.api.global.success.PortfolioHoldingSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeletesResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.dto.request.PurchaseHistoryCreateRequest;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class PortfolioHoldingRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioHoldingService mockedPortfolioHoldingService;

	@Autowired
	private LocalDateTimeService mockedlocalDateTimeService;

	@Autowired
	private PriceRepository currentPriceRepository;

	@Autowired
	private PortfolioHoldingRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	private MockMvc mockMvc;

	public static Stream<Arguments> provideInvalidPortfolioHoldingIds() {
		return Stream.of(
			Arguments.of(Collections.emptyList()),
			Arguments.of((Object)null)
		);
	}

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
	}

	@DisplayName("사용자의 포트폴리오 상세 정보를 가져온다")
	@Test
	void readMyPortfolioHoldings() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
		Stock stock = TestDataFactory.createSamsungStock();
		TestDataFactory.createStockDividend(stock.getTickerSymbol()).forEach(stock::addStockDividend);
		Stock saveStock = stockRepository.save(stock);
		currentPriceRepository.savePrice(saveStock, 60_000L);
		closingPriceRepository.addPrice(saveStock.getTickerSymbol(), 59_000L);

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			TestDataFactory.createPortfolioHolding(portfolio, saveStock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 11, 1, 9, 30, 0);
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(
			TestDataFactory.createPurchaseHistory(purchaseDate.toLocalDate(), portfolioHolding));

		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolio.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(OK_READ_PORTFOLIO_HOLDING.getMessage())))
			.andExpect(jsonPath("data.portfolioDetails.id").value(equalTo(portfolio.getId().intValue())))
			.andExpect(jsonPath("data.portfolioDetails.securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolioDetails.name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolioDetails.budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolioDetails.targetGain").value(equalTo(1500000)))
			.andExpect(jsonPath("data.portfolioDetails.targetReturnRate").value(closeTo(50.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLoss").value(equalTo(900000)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLossRate").value(closeTo(10.00, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.currentValuation").value(equalTo(300000)))
			.andExpect(jsonPath("data.portfolioDetails.investedAmount").value(equalTo(50000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGain").value(equalTo(250000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGainRate").value(closeTo(500.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.dailyGain").value(equalTo(250000)))
			.andExpect(jsonPath("data.portfolioDetails.dailyGainRate").value(closeTo(500.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.balance").value(equalTo(950000)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividend").value(equalTo(5415)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividendYield").value(closeTo(1.81, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.annualInvestmentDividendYield").value(closeTo(10.83, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.provisionalLossBalance").value(equalTo(0)))
			.andExpect(jsonPath("data.portfolioDetails.targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolioDetails.maxLossNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolioHoldings[0].companyName").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.portfolioHoldings[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.portfolioHoldings[0].id").value(equalTo(portfolioHolding.getId().intValue())))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentValuation").value(equalTo(300000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].averageCostPerShare").value(equalTo(10000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].numShares").value(equalTo(5)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChange").value(equalTo(1000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChangeRate").value(closeTo(1.69, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalGain").value(equalTo(250000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalReturnRate").value(closeTo(500, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividend").value(equalTo(5415)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividendYield").value(closeTo(1.81, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dateAdded").value(notNullValue()))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseHistoryId")
				.value(equalTo(purchaseHistory.getId().intValue())))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseDate")
				.value(notNullValue()))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].numShares")
				.value(equalTo(5)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchasePricePerShare")
				.value(equalTo(10000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].memo")
				.value(equalTo("첫구매")));
	}

	@DisplayName("존재하지 않는 포트폴리오 번호를 가지고 포트폴리오 상세 정보를 가져올 수 없다")
	@Test
	void readPortfolioHoldings_whenNotExistPortfolioId_thenResponseNotFound() throws Exception {
		// given
		Long portfolioId = 9999L;
		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolioId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.NOT_FOUND.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.NOT_FOUND.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(ErrorCode.PORTFOLIO_NOT_FOUND.getMessage())))
			.andExpect(jsonPath("data").value(portfolioId.toString()));
	}

	@DisplayName("사용자는 포트폴리오에 종목과 매입이력을 추가한다")
	@Test
	void createPortfolioHolding() throws Exception {
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(TestDataFactory.createSamsungStock());

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = new PurchaseHistoryCreateRequest(
			LocalDateTime.now(),
			Count.from(10L),
			Money.won(100.0),
			null
		);
		PortfolioHoldingCreateRequest request = new PortfolioHoldingCreateRequest(
			stock.getTickerSymbol(),
			purchaseHistoryCreateRequest
		);
		// when & then
		mockMvc.perform(post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_ADD_PORTFOLIO_HOLDING.getMessage())))
			.andExpect(jsonPath("data.portfolioHoldingId").value(greaterThan(0)));
	}

	@DisplayName("사용자는 포트폴리오 종목을 입력하고 매입 이력 정보를 일부만 입력하는 경우 포트폴리오 종목만 저장된다")
	@Test
	void createPortfolioHolding_whenPurchaseHistoryIsNotComplete_thenOnlySavePortfolioHolding() throws Exception {
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(TestDataFactory.createSamsungStock());

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = new PurchaseHistoryCreateRequest(
			null,
			Count.from(10L),
			Money.won(100.0),
			null
		);
		PortfolioHoldingCreateRequest request = new PortfolioHoldingCreateRequest(
			stock.getTickerSymbol(),
			purchaseHistoryCreateRequest
		);
		// when & then
		mockMvc.perform(post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_ADD_PORTFOLIO_HOLDING.getMessage())))
			.andExpect(jsonPath("data.portfolioHoldingId").value(greaterThan(0)));
	}

	@DisplayName("사용자는 포트폴리오에 종목만 추가한다")
	@Test
	void createPortfolioHolding_whenPurchaseHistoryCreateRequestIsNull_thenSavePortfolioHolding() throws Exception {
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(TestDataFactory.createSamsungStock());

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = null;
		PortfolioHoldingCreateRequest request = new PortfolioHoldingCreateRequest(
			stock.getTickerSymbol(),
			purchaseHistoryCreateRequest
		);
		// when & then
		mockMvc.perform(post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.CREATED.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.CREATED.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(CREATED_ADD_PORTFOLIO_HOLDING.getMessage())))
			.andExpect(jsonPath("data.portfolioHoldingId").value(greaterThan(0)));
	}

	@DisplayName("사용자는 포트폴리오에 종목을 추가할때 tickerSymbol을 필수로 같이 전송해야 한다")
	@Test
	void createPortfolioHolding_whenTickerSymbolIsNull_thenResponseError() throws Exception {
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		stockRepository.save(TestDataFactory.createSamsungStock());

		PurchaseHistoryCreateRequest purchaseHistoryCreateRequest = null;
		PortfolioHoldingCreateRequest request = new PortfolioHoldingCreateRequest(
			null,
			purchaseHistoryCreateRequest
		);

		// when & then
		mockMvc.perform(post("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[*].field", containsInAnyOrder("tickerSymbol")))
			.andExpect(jsonPath("data[*].defaultMessage", containsInAnyOrder("티커심볼은 필수 정보입니다")));
	}

	@DisplayName("사용자는 포트폴리오 종목을 삭제한다")
	@Test
	void deletePortfolioStock() throws Exception {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);

		Long portfolioHoldingId = portfolioHolding.getId();
		Long portfolioId = portfolio.getId();

		// when & then
		mockMvc.perform(
				delete("/api/portfolio/{portfolioId}/holdings/{portfolioHoldingId}", portfolioId, portfolioHoldingId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오 종목을 다수 삭제한다")
	@Test
	void deletePortfolioStocks() throws Exception {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);

		List<Long> delPortfolioHoldingIds = List.of(1L, 2L);
		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", delPortfolioHoldingIds);
		String body = ObjectMapperUtil.serialize(requestBodyMap);

		PortfolioStockDeletesResponse mockResponse = new PortfolioStockDeletesResponse(delPortfolioHoldingIds);
		given(mockedPortfolioHoldingService.deletePortfolioHoldings(anyLong(), anyLong(), anyList())).willReturn(
			mockResponse);
		// when & then
		mockMvc.perform(delete("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목들이 삭제되었습니다")))
			.andExpect(jsonPath("data").value(equalTo(null)));
	}

	@DisplayName("사용자는 포트폴리오 종목을 다수 삭제할때 유효하지 않은 입력으로 삭제할 수 없다")
	@MethodSource(value = "provideInvalidPortfolioHoldingIds")
	@ParameterizedTest
	void deletePortfolioStocks_withInvalidItems(List<Long> portfolioHoldingIds) throws Exception {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("portfolioHoldingIds", portfolioHoldingIds);
		String body = ObjectMapperUtil.serialize(requestBodyMap);

		// when & then
		mockMvc.perform(delete("/api/portfolio/{portfolioId}/holdings", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("portfolioHoldingIds")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("삭제할 포트폴리오 종목들이 없습니다")));
	}

	@DisplayName("사용자는 포트폴레오에 대한 차트 정보를 조회한다")
	@Test
	void readMyPortfolioCharts() throws Exception {
		// given
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 60_000L);
		TestDataFactory.createStockDividend(stock.getTickerSymbol()).forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		portfolio.addHolding(portfolioHolding);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		portfolioHolding.addPurchaseHistory(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/charts", portfolio.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오에 대한 차트 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolioDetails.id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioDetails.securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolioDetails.name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.pieChart[0].name").value(equalTo("현금")))
			.andExpect(jsonPath("data.pieChart[0].valuation").value(equalTo(850000)))
			.andExpect(jsonPath("data.pieChart[0].weight").value(equalTo(82.52)))
			.andExpect(jsonPath("data.pieChart[0].totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.pieChart[0].totalGainRate").value(equalTo(0.00)))
			.andExpect(jsonPath("data.pieChart[1].name").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.pieChart[1].valuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.pieChart[1].weight").value(equalTo(17.48)))
			.andExpect(jsonPath("data.pieChart[1].totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.pieChart[1].totalGainRate").value(equalTo(20.0)))
			.andExpect(jsonPath("data.dividendChart[0].month").value(equalTo(1)))
			.andExpect(jsonPath("data.dividendChart[0].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[1].month").value(equalTo(2)))
			.andExpect(jsonPath("data.dividendChart[1].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[2].month").value(equalTo(3)))
			.andExpect(jsonPath("data.dividendChart[2].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[3].month").value(equalTo(4)))
			.andExpect(jsonPath("data.dividendChart[3].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[4].month").value(equalTo(5)))
			.andExpect(jsonPath("data.dividendChart[4].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[5].month").value(equalTo(6)))
			.andExpect(jsonPath("data.dividendChart[5].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[6].month").value(equalTo(7)))
			.andExpect(jsonPath("data.dividendChart[6].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[7].month").value(equalTo(8)))
			.andExpect(jsonPath("data.dividendChart[7].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[8].month").value(equalTo(9)))
			.andExpect(jsonPath("data.dividendChart[8].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[9].month").value(equalTo(10)))
			.andExpect(jsonPath("data.dividendChart[9].amount").value(equalTo(0)))
			.andExpect(jsonPath("data.dividendChart[10].month").value(equalTo(11)))
			.andExpect(jsonPath("data.dividendChart[10].amount").value(equalTo(1083)))
			.andExpect(jsonPath("data.dividendChart[11].month").value(equalTo(12)))
			.andExpect(jsonPath("data.dividendChart[11].amount").value(equalTo(0)));
	}
}
