package co.fineants.api.domain.holding.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioChartResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeleteResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeletesResponse;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.client.KisCurrentPrice;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.api.global.errors.exception.business.HoldingNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import co.fineants.support.cache.PortfolioCacheSupportService;

class PortfolioHoldingServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioHoldingService service;

	@Autowired
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PriceRepository priceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	@Autowired
	private PortfolioCacheSupportService portfolioCacheSupportService;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@AfterEach
	void tearDown() {
		portfolioCacheSupportService.clear();
	}

	@DisplayName("포트폴리오 종목들의 상세 정보를 조회한다")
	@Test
	void readMyPortfolioStocks() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		portfolio.setLocalDateTimeService(spyLocalDateTimeService);
		Stock samsung = createSamsungStock();
		createStockDividendThisYearWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);
		Stock stock = stockRepository.save(samsung);
		given(spyLocalDateTimeService.getLocalDateWithNow()).willReturn(LocalDate.of(2024, 1, 1));
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		priceRepository.savePrice(KisCurrentPrice.create("005930", 60000L));
		closingPriceRepository.savePrice("005930", 50000);

		setAuthentication(member);
		// when
		PortfolioHoldingsResponse response = service.readPortfolioHoldings(portfolio.getId());

		// then
		PortfolioDetailResponse details = response.getPortfolioDetails();
		Expression pureTargetGain = Money.won(500000);
		Expression budget = Money.won(1000000);
		Percentage targetReturnRate = RateDivision.of(pureTargetGain, budget)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		Expression pureMaximumLoss = Money.won(100000);
		Percentage maximumLossRate = RateDivision.of(pureMaximumLoss, budget)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		Expression totalGain = Money.won(30000);
		Expression totalInvestmentAmount = Money.won(150000);
		Percentage totalGainRate = RateDivision.of(totalGain, totalInvestmentAmount)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		Expression dailyGain = Money.won(30000);
		Percentage dailyGainRate = RateDivision.of(dailyGain, totalInvestmentAmount)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		Money totalAnnualDividend = Money.won(361 * 3 * 3);
		Expression currentValuation = Money.won(180000);
		Percentage annualDividendYield = RateDivision.of(totalAnnualDividend, currentValuation)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		assertAll(
			() -> assertThat(details.getSecuritiesFirm()).isEqualTo("토스증권"),
			() -> assertThat(details.getName()).isEqualTo("내꿈은 워렌버핏"),
			() -> assertThat(details.getBudget()).isEqualByComparingTo(Money.won(1000000L)),
			() -> assertThat(details.getTargetGain()).isEqualByComparingTo(Money.won(1500000L)),
			() -> assertThat(details.getTargetReturnRate()).isEqualByComparingTo(targetReturnRate),
			() -> assertThat(details.getMaximumLoss()).isEqualByComparingTo(Money.won(900000L)),
			() -> assertThat(details.getMaximumLossRate()).isEqualByComparingTo(maximumLossRate),
			() -> assertThat(details.getInvestedAmount()).isEqualByComparingTo(Money.won(150000L)),
			() -> assertThat(details.getTotalGain()).isEqualByComparingTo(Money.won(30000L)),
			() -> assertThat(details.getTotalGainRate()).isEqualByComparingTo(totalGainRate),
			() -> assertThat(details.getDailyGain()).isEqualByComparingTo(Money.won(30000L)),
			() -> assertThat(details.getDailyGainRate()).isEqualByComparingTo(dailyGainRate),
			() -> assertThat(details.getBalance()).isEqualByComparingTo(Money.won(850000L)),
			() -> assertThat(details.getAnnualDividend()).isEqualByComparingTo(totalAnnualDividend),
			() -> assertThat(details.getAnnualDividendYield()).isEqualByComparingTo(annualDividendYield),
			() -> assertThat(details.getProvisionalLossBalance()).isEqualByComparingTo(Money.won(0L)),
			() -> assertThat(details.getTargetGainNotify()).isTrue(),
			() -> assertThat(details.getMaxLossNotify()).isTrue(),

			() -> assertThat(response)
				.extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.extracting("stock")
				.extracting("companyName", "tickerSymbol")
				.containsExactlyInAnyOrder(Tuple.tuple("삼성전자보통주", "005930")),

			() -> assertThat(response)
				.extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.extracting("portfolioHolding")
				.extracting("id", "currentValuation", "averageCostPerShare",
					"numShares", "dailyChange", "dailyChangeRate", "totalGain", "totalReturnRate", "annualDividend")
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Count::compareTo, Count.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple(
						portfolioHolding.getId(),
						Money.won(180000),
						Money.won(50000),
						Count.from(3L),
						Money.won(10000),
						Percentage.from(0.2),
						Money.won(30000),
						Percentage.from(0.2),
						Money.won(3249)
					)
				),
			() -> assertThat(response)
				.extracting("portfolioHoldings")
				.asList()
				.hasSize(1)
				.flatExtracting("purchaseHistory")
				.extracting("purchaseDate", "numShares", "purchasePricePerShare", "memo")
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Count::compareTo, Count.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple(
						LocalDateTime.of(2023, 9, 26, 9, 30, 0),
						Count.from(3L),
						Money.won(50000.0),
						"첫구매"
					)
				)
		);
	}

	@DisplayName("회원은 다른 회원의 포트폴리오 종목들을 읽을 수 없다")
	@Test
	void readMyPortfolioStocks_whenReadOtherMemberHolding_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.readPortfolioHoldings(portfolio.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(portfolio.toString());
	}

	@DisplayName("사용자는 포트폴리오의 차트 정보를 조회한다")
	@Test
	void readMyPortfolioCharts() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock samsung = createSamsungStock();
		createStockDividendWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);
		Stock stock = stockRepository.save(samsung);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		priceRepository.savePrice(KisCurrentPrice.create("005930", 60000L));
		closingPriceRepository.savePrice("005930", 50000);

		setAuthentication(member);
		// when
		PortfolioChartResponse response = service.readPortfolioCharts(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("portfolioDetails")
				.extracting("id", "securitiesFirm", "name")
				.containsExactly(portfolio.getId(), "토스증권", "내꿈은 워렌버핏"),
			() -> assertThat(response)
				.extracting("pieChart")
				.asList()
				.hasSize(2)
				.extracting("name", "valuation", "weight", "totalGain", "totalGainRate")
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple("현금", Money.won(850000L), Percentage.from(0.8252), Money.zero(), Percentage.zero()),
					Tuple.tuple("삼성전자보통주", Money.won(180000L), Percentage.from(0.1748), Money.won(30000L),
						Percentage.from(0.2))
				),
			() -> assertThat(response)
				.extracting("dividendChart")
				.asList()
				.hasSize(12)
				.extracting("month", "amount")
				.usingComparatorForType(Money::compareTo, Money.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple(1, Money.zero()),
					Tuple.tuple(2, Money.zero()),
					Tuple.tuple(3, Money.zero()),
					Tuple.tuple(4, Money.zero()),
					Tuple.tuple(5, Money.zero()),
					Tuple.tuple(6, Money.zero()),
					Tuple.tuple(7, Money.zero()),
					Tuple.tuple(8, Money.zero()),
					Tuple.tuple(9, Money.zero()),
					Tuple.tuple(10, Money.zero()),
					Tuple.tuple(11, Money.won(1083L)),
					Tuple.tuple(12, Money.zero())
				),
			() -> assertThat(response.getSectorChart())
				.extracting(PortfolioSectorChartItem::getSector, PortfolioSectorChartItem::getSectorWeight)
				.containsExactlyInAnyOrder(
					Tuple.tuple("현금", Percentage.from(0.8252)),
					Tuple.tuple("전기전자", Percentage.from(0.1748))
				)
		);
	}

	@DisplayName("사용자는 예산이 0원인 상태의 포트폴리오의 차트를 조회한다")
	@Test
	void readMyPortfolioCharts_whenPortfolioBudgetIsZero_thenOK() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member, Money.zero()));
		Stock stock = stockRepository.save(createSamsungStock());
		List<StockDividend> stockDividends = createStockDividendWith(stock.getTickerSymbol());
		stockDividends.forEach(stock::addStockDividend);

		setAuthentication(member);
		// when
		PortfolioChartResponse response = service.readPortfolioCharts(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getPortfolioDetails())
				.extracting("id", "securitiesFirm", "name")
				.containsExactly(portfolio.getId(), "토스증권", "내꿈은 워렌버핏"),
			() -> assertThat(response.getPieChart())
				.asList()
				.hasSize(1)
				.extracting("name", "valuation", "weight", "totalGain", "totalGainRate")
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple("현금", Money.zero(), Percentage.zero(), Money.zero(), Percentage.zero())
				),
			() -> assertThat(response.getDividendChart())
				.asList()
				.isEmpty(),
			() -> assertThat(response.getSectorChart())
				.asList()
				.hasSize(1)
				.extracting("sector", "sectorWeight")
				.containsExactlyInAnyOrder(Tuple.tuple("현금", Percentage.zero()))
		);
	}

	@DisplayName("회원은 다른 회원의 포트폴리오 차트를 조회할 수 없다")
	@Test
	void readMyPortfolioCharts_whenOtherMemberRead_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		List<StockDividend> stockDividends = createStockDividendWith(stock.getTickerSymbol());
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> service.readPortfolioCharts(portfolio.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(portfolio.toString());
	}

	@DisplayName("사용자는 포트폴리오에 실시간 상세 데이터를 조회한다")
	@Test
	void readMyPortfolioStocksInRealTime() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createKakaoStock());
		List<StockDividend> stockDividends = createStockDividendWith(stock.getTickerSymbol());
		stockDividends.forEach(stock::addStockDividend);
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));
		PortfolioHolding portfolioHolding2 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding2));
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding2));

		priceRepository.savePrice(KisCurrentPrice.create("005930", 60000L));
		priceRepository.savePrice(KisCurrentPrice.create("035720", 60000L));
		closingPriceRepository.savePrice("005930", 50000);
		closingPriceRepository.savePrice("035720", 50000);

		// when
		StreamMessage portfolioStreamMessage = service.getPortfolioReturns(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(portfolioStreamMessage)
				.extracting("portfolioDetails")
				.extracting("currentValuation", "totalGain", "totalGainRate", "dailyGain", "dailyGainRate",
					"provisionalLossBalance")
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(Money.won(720000L), Money.won(120000L), Percentage.from(0.2),
					Money.won(120000L), Percentage.from(0.2), Money.zero()),

			() -> assertThat(portfolioStreamMessage)
				.extracting("portfolioHoldings")
				.asList()
				.hasSize(2)
				.extracting("currentValuation", "dailyChange", "dailyChangeRate", "totalGain",
					"totalReturnRate")
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple(Money.won(360000L), Money.won(10000L), Percentage.from(0.2),
						Money.won(60000L),
						Percentage.from(0.2)),
					Tuple.tuple(Money.won(360000L), Money.won(10000L), Percentage.from(0.2),
						Money.won(60000L),
						Percentage.from(0.2)))
		);
	}

	@DisplayName("사용자는 포트폴리오의 종목을 삭제한다")
	@Test
	void deletePortfolioStock() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());

		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(
			PortfolioHolding.of(portfolio, stock)
		);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(1);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		Long portfolioHoldingId = portfolioHolding.getId();
		setAuthentication(member);
		// when
		PortfolioStockDeleteResponse response = service.deletePortfolioStock(portfolioHoldingId, portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response).extracting("portfolioHoldingId").isNotNull(),
			() -> assertThat(portfolioHoldingRepository.findById(portfolioHoldingId)).isEmpty(),
			() -> assertThat(purchaseHistoryRepository.findAllByPortfolioHoldingId(portfolioHoldingId)).isEmpty(),
			() -> assertThat(portfolioCacheSupportService.fetchCache().get(portfolio.getId())).isNull()
		);
	}

	@DisplayName("사용자는 다수의 포트폴리오 종목을 삭제할 수 있다")
	@Test
	void deletePortfolioStocks() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createSamsungStock());
		Stock stock2 = stockRepository.save(createDongwhaPharmStock());
		PortfolioHolding portfolioHolding1 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));
		PortfolioHolding portfolioHolding2 = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock2));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding1));
		PurchaseHistory purchaseHistory2 = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding2));
		List<Long> portfolioHoldingIds = List.of(portfolioHolding1.getId(), portfolioHolding2.getId());

		setAuthentication(member);
		// when
		PortfolioStockDeletesResponse response = service.deletePortfolioHoldings(portfolio.getId(), member.getId(),
			portfolioHoldingIds);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("portfolioHoldingIds")
				.asList()
				.hasSize(2)
				.containsExactlyInAnyOrder(portfolioHolding1.getId(), portfolioHolding2.getId()),
			() -> assertThat(purchaseHistoryRepository.existsById(purchaseHistory1.getId())).isFalse(),
			() -> assertThat(purchaseHistoryRepository.existsById(purchaseHistory2.getId())).isFalse(),
			() -> assertThat(portfolioHoldingRepository.existsById(portfolioHolding1.getId())).isFalse(),
			() -> assertThat(portfolioHoldingRepository.existsById(portfolioHolding2.getId())).isFalse(),
			() -> assertThat(portfolioCacheSupportService.fetchCache().get(portfolio.getId())).isNull()
		);
	}

	@DisplayName("사용자는 다수의 포트폴리오 삭제시 존재하지 않는 일부 포트폴리오 종목이 존재한다면 전부 삭제할 수 없다")
	@Test
	void deletePortfolioStocks_whenNotExistPortfolioHolding_thenError404() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));
		List<Long> portfolioHoldingIds = List.of(portfolioHolding.getId(), 9999L);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePortfolioHoldings(portfolio.getId(), member.getId(), portfolioHoldingIds));

		// then
		assertThat(throwable)
			.isInstanceOf(HoldingNotFoundException.class)
			.hasMessage("9999");
		assertThat(portfolioHoldingRepository.findById(portfolioHolding.getId())).isPresent();
		assertThat(purchaseHistoryRepository.findById(purchaseHistory.getId())).isPresent();
	}

	@DisplayName("사용자는 다수의 포트폴리오 삭제시 다른 회원의 포트폴리오 종목이 존재한다면 전부 삭제할 수 없다")
	@Test
	void deletePortfolioStocks_whenNotExistPortfolioHolding_thenError403() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock1 = stockRepository.save(createSamsungStock());
		PortfolioHolding portfolioHolding = portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock1));

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory = purchaseHistoryRepository.save(
			createPurchaseHistory(null, purchaseDate, numShares, purchasePerShare, memo, portfolioHolding));

		Member member2 = memberRepository.save(createMember("일개미2222", "user2@gmail.com"));
		Portfolio portfolio2 = portfolioRepository.save(createPortfolio(member2));
		PortfolioHolding portfolioHolding2 = portfolioHoldingRepository.save(
			createPortfolioHolding(portfolio2, stock1));
		List<Long> portfolioHoldingIds = List.of(portfolioHolding.getId(), portfolioHolding2.getId());

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePortfolioHoldings(portfolio.getId(), member.getId(), portfolioHoldingIds));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(portfolioHolding2.toString());
		assertThat(portfolioHoldingRepository.findById(portfolioHolding.getId())).isPresent();
		assertThat(portfolioHoldingRepository.findById(portfolioHolding2.getId())).isPresent();
		assertThat(purchaseHistoryRepository.findById(purchaseHistory.getId())).isPresent();
	}

	@DisplayName("사용자는 매입이력 없이 포트폴리오 종목을 추가할 수 있다")
	@Test
	void givenRequest_whenOnlyTickerSymbol_thenReturnResponse() {
		// given
		Stock samsung = stockRepository.save(createSamsungStock());
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));

		PortfolioHolding holding = PortfolioHolding.of(portfolio, samsung);
		// when
		service.savePortfolioHolding(holding);
		// then
		List<PortfolioHolding> holdings = portfolioHoldingRepository.findAll();
		assertThat(holdings).hasSize(1);
	}

	@DisplayName("포트폴리오 종목이 없으면 빈 Optional을 반환한다")
	@Test
	void getPortfolioHoldingBy_givenPortfolioAndStock_whenNotExistPortfolioHolding_thenReturnEmptyOptional() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());
		// when
		Optional<PortfolioHolding> holding = service.getPortfolioHoldingBy(portfolio, stock);
		// then
		assertThat(holding).isEmpty();
	}

	@DisplayName("포트폴리오 종목을 조회한다")
	@Test
	void getPortfolioHoldingBy_givenPortfolioAndStock_whenExistPortfolioHolding_thenReturnHoldingOptional() {
		// given
		Member member = memberRepository.save(createMember());
		Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
		Stock stock = stockRepository.save(createSamsungStock());

		portfolioHoldingRepository.save(PortfolioHolding.of(portfolio, stock));
		// when
		Optional<PortfolioHolding> holding = service.getPortfolioHoldingBy(portfolio, stock);
		// then
		assertThat(holding).isPresent();
	}
}
