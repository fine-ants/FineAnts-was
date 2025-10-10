package co.fineants.api.domain.holding.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.holding.domain.chart.DividendChart;
import co.fineants.api.domain.holding.domain.chart.PieChart;
import co.fineants.api.domain.holding.domain.chart.SectorChart;
import co.fineants.api.domain.holding.domain.dto.request.PortfolioHoldingCreateRequest;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioChartResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetails;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeletesResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.domain.factory.PortfolioSseEmitterFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamMessageConsumerFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioStreamerFactory;
import co.fineants.api.domain.holding.domain.factory.SseEventBuilderFactory;
import co.fineants.api.domain.holding.event.publisher.PortfolioHoldingEventPublisher;
import co.fineants.api.domain.holding.service.PortfolioHoldingFacade;
import co.fineants.api.domain.holding.service.PortfolioHoldingService;
import co.fineants.api.domain.kis.repository.CurrentPriceMemoryRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.service.PortfolioCacheService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.PortfolioNotFoundException;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.support.controller.ControllerTestSupport;

class PortfolioHoldingRestControllerTest extends ControllerTestSupport {

	@Autowired
	private PortfolioHoldingService mockedPortfolioHoldingService;

	@Autowired
	private LocalDateTimeService mockedlocalDateTimeService;

	private PriceRepository currentPriceRepository;
	private PortfolioCalculator calculator;
	private PortfolioHoldingFacade portfolioHoldingFacade;

	public static Stream<Arguments> provideInvalidPortfolioHoldingIds() {
		return Stream.of(
			Arguments.of(Collections.emptyList()),
			Arguments.of((Object)null)
		);
	}

	@Override
	protected Object initController() {
		PortfolioStreamerFactory portfolioStreamerFactory = mock(PortfolioStreamerFactory.class);
		PortfolioStreamMessageConsumerFactory portfolioStreamMessageConsumerFactory = mock(
			PortfolioStreamMessageConsumerFactory.class);
		PortfolioSseEmitterFactory portfolioSseEmitterFactory = mock(PortfolioSseEmitterFactory.class);
		SseEventBuilderFactory portfolioSseEventBuilderFactory = mock(SseEventBuilderFactory.class);
		PortfolioCacheService portfolioCacheService = mock(PortfolioCacheService.class);
		PortfolioHoldingEventPublisher portfolioHoldingEventPublisher = mock(PortfolioHoldingEventPublisher.class);
		portfolioHoldingFacade = mock(PortfolioHoldingFacade.class);
		return new PortfolioHoldingRestController(
			mockedPortfolioHoldingService,
			portfolioStreamerFactory,
			portfolioStreamMessageConsumerFactory,
			portfolioSseEmitterFactory,
			portfolioSseEventBuilderFactory,
			portfolioCacheService,
			portfolioHoldingEventPublisher,
			portfolioHoldingFacade
		);
	}

	@BeforeEach
	void setUp() {
		currentPriceRepository = new CurrentPriceMemoryRepository();
		calculator = new PortfolioCalculator(currentPriceRepository, mockedlocalDateTimeService);
	}

	@DisplayName("사용자의 포트폴리오 상세 정보를 가져온다")
	@Test
	void readMyPortfolioStocks() throws Exception {
		// given
		given(mockedlocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 1, 1));
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();
		currentPriceRepository.savePrice(stock, 60_000L);
		TestDataFactory.createStockDividend(stock.getTickerSymbol()).forEach(stock::addStockDividendTemp);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 11, 1, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		portfolioHolding.addPurchaseHistory(
			createPurchaseHistory(1L, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		portfolio.addHolding(portfolioHolding);
		PortfolioGainHistory history = createEmptyPortfolioGainHistory(portfolio);
		Map<String, Money> lastDayClosingPriceMap = Map.of("005930", Money.won(50000L));
		PortfolioHoldingsResponse mockResponse = PortfolioHoldingsResponse.of(portfolio, history,
			List.of(portfolioHolding),
			lastDayClosingPriceMap,
			mockedlocalDateTimeService,
			calculator);

		given(mockedPortfolioHoldingService.readPortfolioHoldings(anyLong())).willReturn(mockResponse);
		// when & then
		ResultActions resultActions = mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolio.getId()))
			.andExpect(status().isOk());

		resultActions
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 상세 정보 및 포트폴리오 종목 목록 조회가 완료되었습니다")))
			.andExpect(jsonPath("data.portfolioDetails.id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioDetails.securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolioDetails.name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolioDetails.budget").value(equalTo(1000000)))
			.andExpect(jsonPath("data.portfolioDetails.targetGain").value(equalTo(1500000)))
			.andExpect(jsonPath("data.portfolioDetails.targetReturnRate").value(closeTo(50.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLoss").value(equalTo(900000)))
			.andExpect(jsonPath("data.portfolioDetails.maximumLossRate").value(closeTo(10.00, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.currentValuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.portfolioDetails.investedAmount").value(equalTo(150000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.portfolioDetails.totalGainRate").value(closeTo(20.0, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.balance").value(equalTo(850000)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividend").value(equalTo(4332)))
			.andExpect(jsonPath("data.portfolioDetails.annualDividendYield").value(closeTo(2.41, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.annualInvestmentDividendYield").value(closeTo(2.89, 0.1)))
			.andExpect(jsonPath("data.portfolioDetails.provisionalLossBalance").value(equalTo(0)))
			.andExpect(jsonPath("data.portfolioDetails.targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolioDetails.maxLossNotify").value(equalTo(true)));

		resultActions
			.andExpect(jsonPath("data.portfolioHoldings[0].companyName").value(equalTo("삼성전자보통주")))
			.andExpect(jsonPath("data.portfolioHoldings[0].tickerSymbol").value(equalTo("005930")))
			.andExpect(jsonPath("data.portfolioHoldings[0].id").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentValuation").value(equalTo(180000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].currentPrice").value(equalTo(60000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].averageCostPerShare").value(equalTo(50000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].numShares").value(equalTo(3)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChange").value(equalTo(10000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].dailyChangeRate").value(closeTo(20.0, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalGain").value(equalTo(30000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].totalReturnRate").value(closeTo(20.0, 0.1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividend").value(equalTo(4332)))
			.andExpect(jsonPath("data.portfolioHoldings[0].annualDividendYield").value(closeTo(2.41, 0.1)));

		resultActions
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseHistoryId").value(equalTo(1)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchaseDate").value(
				equalTo("2023-11-01T09:30:00")))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].numShares").value(equalTo(3)))
			.andExpect(
				jsonPath("data.portfolioHoldings[0].purchaseHistory[0].purchasePricePerShare").value(equalTo(50000)))
			.andExpect(jsonPath("data.portfolioHoldings[0].purchaseHistory[0].memo").value(equalTo("첫구매")));
	}

	@DisplayName("존재하지 않는 포트폴리오 번호를 가지고 포트폴리오 상세 정보를 가져올 수 없다")
	@Test
	void readMyPortfolioStocksWithNotExistPortfolioId() throws Exception {
		// given
		Long portfolioId = 9999L;
		given(mockedPortfolioHoldingService.readPortfolioHoldings(anyLong()))
			.willThrow(new PortfolioNotFoundException(portfolioId.toString()));

		// when & then
		mockMvc.perform(get("/api/portfolio/{portfolioId}/holdings", portfolioId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("message").value(equalTo("Portfolio Not Found")));
	}

	@DisplayName("사용자는 포트폴리오에 종목과 매입이력을 추가한다")
	@Test
	void addPortfolioStock() throws Exception {
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();

		PortfolioHolding holding = PortfolioHolding.of(1L, portfolio, stock);
		given(portfolioHoldingFacade.createPortfolioHolding(any(PortfolioHoldingCreateRequest.class), anyLong()))
			.willReturn(holding);

		Map<String, Object> purchaseHistoryMap = new HashMap<>();
		purchaseHistoryMap.put("purchaseDate", LocalDateTime.now().toString());
		purchaseHistoryMap.put("numShares", 10L);
		purchaseHistoryMap.put("purchasePricePerShare", 100.0);
		purchaseHistoryMap.put("memo", null);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");
		requestBodyMap.put("purchaseHistory", purchaseHistoryMap);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();
		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 추가되었습니다")))
			.andExpect(jsonPath("data.portfolioHoldingId").value(equalTo(1)));
	}

	@DisplayName("사용자는 포트폴리오 종목을 입력하고 매입 이력 정보를 일부만 입력하는 경우 포트폴리오 종목만 저장된다")
	@Test
	void savePortfolioHolding_whenPurchaseHistoryIsNotComplete_thenThrowException() throws Exception {
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();

		PortfolioHolding holding = PortfolioHolding.of(1L, portfolio, stock);
		given(portfolioHoldingFacade.createPortfolioHolding(any(PortfolioHoldingCreateRequest.class), anyLong()))
			.willReturn(holding);

		Map<String, Object> purchaseHistoryMap = new HashMap<>();
		purchaseHistoryMap.put("purchaseDate", null); // 매입 날짜가 입력되지 않음
		purchaseHistoryMap.put("numShares", 10L);
		purchaseHistoryMap.put("purchasePricePerShare", 100.0);
		purchaseHistoryMap.put("memo", null);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");
		requestBodyMap.put("purchaseHistory", purchaseHistoryMap);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();
		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 추가되었습니다")))
			.andExpect(jsonPath("data.portfolioHoldingId").value(equalTo(1)));
	}

	@DisplayName("사용자는 포트폴리오에 종목만 추가한다")
	@Test
	void addPortfolioStockOnly() throws Exception {
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);
		Stock stock = createSamsungStock();

		PortfolioHolding holding = PortfolioHolding.of(1L, portfolio, stock);
		given(portfolioHoldingFacade.createPortfolioHolding(any(PortfolioHoldingCreateRequest.class), anyLong()))
			.willReturn(holding);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", "005930");

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();
		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 종목이 추가되었습니다")))
			.andExpect(jsonPath("data.portfolioHoldingId").value(equalTo(1)));
	}

	@DisplayName("사용자는 포트폴리오에 종목을 추가할때 stockId를 필수로 같이 전송해야 한다")
	@Test
	void addPortfolioStockWithStockIdIsNull() throws Exception {
		Member member = TestDataFactory.createMember();
		Portfolio portfolio = createPortfolio(member);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("tickerSymbol", null);

		String body = ObjectMapperUtil.serialize(requestBodyMap);
		Long portfolioId = portfolio.getId();

		// when & then
		mockMvc.perform(post("/api/portfolio/" + portfolioId + "/holdings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data").isArray());
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
		TestDataFactory.createStockDividend(stock.getTickerSymbol()).forEach(stock::addStockDividendTemp);
		PortfolioHolding portfolioHolding = createPortfolioHolding(portfolio, stock);
		portfolio.addHolding(portfolioHolding);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		portfolioHolding.addPurchaseHistory(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		PieChart pieChart = new PieChart(calculator);
		DividendChart dividendChart = new DividendChart(calculator);
		SectorChart sectorChart = new SectorChart(calculator);

		PortfolioDetails portfolioDetails = PortfolioDetails.from(portfolio);
		List<PortfolioPieChartItem> pieChartItems = pieChart.createItemsBy(portfolio);
		List<PortfolioDividendChartItem> dividendChartItems = dividendChart.createItemsBy(portfolio,
			LocalDate.of(2024, 1, 16));
		List<PortfolioSectorChartItem> sectorChartItems = sectorChart.createBy(portfolio);
		PortfolioChartResponse response = PortfolioChartResponse.create(portfolioDetails, pieChartItems,
			dividendChartItems, sectorChartItems);
		given(mockedPortfolioHoldingService.readPortfolioCharts(anyLong(), any(LocalDate.class)))
			.willReturn(response);

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
