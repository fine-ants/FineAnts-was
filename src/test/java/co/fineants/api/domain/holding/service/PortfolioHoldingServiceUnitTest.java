package co.fineants.api.domain.holding.service;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.common.money.RateDivision;
import co.fineants.api.domain.holding.domain.chart.DividendChart;
import co.fineants.api.domain.holding.domain.chart.PieChart;
import co.fineants.api.domain.holding.domain.chart.SectorChart;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioChartResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetailResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDetails;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioDividendChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingDetailItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingRealTimeItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsRealTimeResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioHoldingsResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioPieChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioSectorChartItem;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeleteResponse;
import co.fineants.api.domain.holding.domain.dto.response.PortfolioStockDeletesResponse;
import co.fineants.api.domain.holding.domain.dto.response.PurchaseHistoryItem;
import co.fineants.api.domain.holding.domain.dto.response.StockItem;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.holding.domain.factory.PortfolioDetailFactory;
import co.fineants.api.domain.holding.domain.factory.PortfolioHoldingDetailFactory;
import co.fineants.api.domain.holding.domain.message.StreamMessage;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.api.domain.purchasehistory.repository.PurchaseHistoryRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.HoldingNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;

@ExtendWith(MockitoExtension.class)
class PortfolioHoldingServiceUnitTest {
	@InjectMocks
	private PortfolioHoldingService service;

	@Mock
	private PortfolioRepository portfolioRepository;

	@Mock
	private PortfolioDetailFactory portfolioDetailFactory;

	@Mock
	private PortfolioHoldingDetailFactory portfolioHoldingDetailFactory;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Mock
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Mock
	private PieChart pieChart;

	@Mock
	private DividendChart dividendChart;

	@Mock
	private SectorChart sectorChart;

	@Mock
	private PortfolioCalculator portfolioCalculator;

	@Mock
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@DisplayName("포트폴리오 종목들의 상세 정보를 조회한다")
	@Test
	void should_return_detailed_portfolio_response_when_read_my_portfolio_stocks() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();
		TestDataFactory.createStockDividendThisYearWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);

		PortfolioHolding portfolioHolding = TestDataFactory.createPortfolioHolding(portfolio, samsung);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory history = TestDataFactory.createPurchaseHistory(1L, purchaseDate, numShares, purchasePerShare,
			memo, portfolioHolding);

		BDDMockito.given(portfolioRepository.findById(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		PortfolioDetailResponse portfolioDetailResponse = PortfolioDetailResponse.builder()
			.id(portfolio.getId())
			.securitiesFirm("토스증권")
			.name("내꿈은 워렌버핏")
			.budget(Money.won(1_000_000L))
			.targetGain(Money.won(1_500_000L))
			.targetReturnRate(Percentage.from(BigDecimal.valueOf(0.5)))
			.maximumLoss(Money.won(900_000L))
			.maximumLossRate(Percentage.from(BigDecimal.valueOf(0.1)))
			.currentValuation(Money.won(180_000))
			.investedAmount(Money.won(150_000))
			.totalGain(Money.won(30_000L))
			.totalGainRate(Percentage.from(BigDecimal.valueOf(0.2)))
			.dailyGain(Money.won(30_000L))
			.dailyGainRate(Percentage.from(BigDecimal.valueOf(0.2)))
			.balance(Money.won(850_000L))
			.annualDividend(Money.won(3_249))
			.annualDividendYield(Percentage.from(BigDecimal.valueOf(0.0181)))
			.annualInvestmentDividendYield(Percentage.from(BigDecimal.valueOf(0.0217)))
			.provisionalLossBalance(Money.won(0L))
			.targetGainNotify(true)
			.maxLossNotify(true)
			.build();
		BDDMockito.given(portfolioDetailFactory.createPortfolioDetailItem(portfolio))
			.willReturn(portfolioDetailResponse);

		StockItem stockItem = StockItem.from(samsung);
		PortfolioHoldingDetailItem portfolioHoldingDetailItem = PortfolioHoldingDetailItem.builder()
			.id(portfolioHolding.getId())
			.currentValuation(Money.won(180_000))
			.averageCostPerShare(Money.won(50_000))
			.numShares(Count.from(3))
			.dailyChange(Money.won(10_000))
			.dailyChangeRate(Percentage.from(0.2))
			.totalGain(Money.won(30_000))
			.totalReturnRate(Percentage.from(0.2))
			.annualDividend(Money.won(3_249))
			.build();
		PurchaseHistoryItem purchaseHistoryItem = PurchaseHistoryItem.from(history);
		PortfolioHoldingItem portfolioHoldingItem = PortfolioHoldingItem.builder()
			.stock(stockItem)
			.portfolioHolding(portfolioHoldingDetailItem)
			.purchaseHistory(List.of(purchaseHistoryItem))
			.build();
		BDDMockito.given(portfolioHoldingDetailFactory.createPortfolioHoldingItems(portfolio))
			.willReturn(List.of(portfolioHoldingItem));

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
		Expression currentValuation = Money.won(180_000);
		Percentage annualDividendYield = RateDivision.of(totalAnnualDividend, currentValuation)
			.toPercentage(Bank.getInstance(), Currency.KRW);

		assertAll(
			() -> assertThat(details.getSecuritiesFirm()).isEqualTo("토스증권"),
			() -> assertThat(details.getName()).isEqualTo("내꿈은 워렌버핏"),
			() -> assertThat(details.getBudget()).isEqualByComparingTo(Money.won(1_000_000L)),
			() -> assertThat(details.getTargetGain()).isEqualByComparingTo(Money.won(1_500_000L)),
			() -> assertThat(details.getTargetReturnRate()).isEqualByComparingTo(targetReturnRate),
			() -> assertThat(details.getMaximumLoss()).isEqualByComparingTo(Money.won(900_000L)),
			() -> assertThat(details.getMaximumLossRate()).isEqualByComparingTo(maximumLossRate),
			() -> assertThat(details.getInvestedAmount()).isEqualByComparingTo(Money.won(150_000L)),
			() -> assertThat(details.getTotalGain()).isEqualByComparingTo(Money.won(30_000L)),
			() -> assertThat(details.getTotalGainRate()).isEqualByComparingTo(totalGainRate),
			() -> assertThat(details.getDailyGain()).isEqualByComparingTo(Money.won(30_000L)),
			() -> assertThat(details.getDailyGainRate()).isEqualByComparingTo(dailyGainRate),
			() -> assertThat(details.getBalance()).isEqualByComparingTo(Money.won(850_000L)),
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
						Money.won(180_000),
						Money.won(50_000),
						Count.from(3L),
						Money.won(10_000),
						Percentage.from(0.2),
						Money.won(30_000),
						Percentage.from(0.2),
						Money.won(3_249)
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

	@DisplayName("사용자는 포트폴리오의 차트 정보를 조회한다")
	@Test
	void should_return_portfolio_chart_data_when_read_my_portfolio_charts() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		TestDataFactory.createStockDividend(stock.getTickerSymbol()).forEach(stock::addStockDividend);

		BDDMockito.given(portfolioRepository.findById(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		PortfolioPieChartItem cashPieChartItem = PortfolioPieChartItem.cash(
			Percentage.from(BigDecimal.valueOf(0.8252)),
			Money.won(850_000L)
		);
		PortfolioPieChartItem samsungPieChartItem = PortfolioPieChartItem.stock(
			stock.getCompanyName(),
			Money.won(180_000L),
			Percentage.from(0.1748),
			Money.won(30_000L),
			Percentage.from(BigDecimal.valueOf(0.2))
		);
		BDDMockito.given(pieChart.createItemsBy(portfolio))
			.willReturn(List.of(cashPieChartItem, samsungPieChartItem));
		BDDMockito.given(dividendChart.createItemsBy(portfolio))
			.willReturn(List.of(
				PortfolioDividendChartItem.empty(1),
				PortfolioDividendChartItem.empty(2),
				PortfolioDividendChartItem.empty(3),
				PortfolioDividendChartItem.empty(4),
				PortfolioDividendChartItem.empty(5),
				PortfolioDividendChartItem.empty(6),
				PortfolioDividendChartItem.empty(7),
				PortfolioDividendChartItem.empty(8),
				PortfolioDividendChartItem.empty(9),
				PortfolioDividendChartItem.empty(10),
				PortfolioDividendChartItem.create(11, Money.won(1083L)),
				PortfolioDividendChartItem.empty(12)
			));
		BDDMockito.given(sectorChart.createBy(portfolio))
			.willReturn(List.of(
				PortfolioSectorChartItem.create("현금", Percentage.from(0.8252)),
				PortfolioSectorChartItem.create("전기전자", Percentage.from(0.1748))
			));
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
					Tuple.tuple("현금", Money.won(850_000L), Percentage.from(0.8252), Money.zero(), Percentage.zero()),
					Tuple.tuple("삼성전자보통주", Money.won(180_000L), Percentage.from(0.1748), Money.won(30_000L),
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
	void should_return_portfolio_chart_data_when_portfolio_budget_is_zero_then_cash_is_zero() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member, Money.zero());
		Stock stock = TestDataFactory.createSamsungStock();
		List<StockDividend> stockDividends = TestDataFactory.createStockDividend(stock.getTickerSymbol());
		stockDividends.forEach(stock::addStockDividend);

		BDDMockito.given(portfolioRepository.findById(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		PortfolioPieChartItem cashPieChartItem = PortfolioPieChartItem.cash(Percentage.zero(), Money.zero());
		BDDMockito.given(pieChart.createItemsBy(portfolio))
			.willReturn(List.of(cashPieChartItem));
		BDDMockito.given(sectorChart.createBy(portfolio))
			.willReturn(List.of(
				PortfolioSectorChartItem.create("현금", Percentage.zero())
			));

		// when
		PortfolioChartResponse response = service.readPortfolioCharts(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(response.getPortfolioDetails())
				.extracting(PortfolioDetails::getId, PortfolioDetails::getSecuritiesFirm, PortfolioDetails::getName)
				.containsExactly(portfolio.getId(), "토스증권", "내꿈은 워렌버핏"),
			() -> assertThat(response.getPieChart())
				.hasSize(1)
				.extracting(pie -> Tuple.tuple(pie.getName(), pie.getValuation(), pie.getWeight(), pie.getTotalGain(),
					pie.getTotalGainRate()))
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple("현금", Money.zero(), Percentage.zero(), Money.zero(), Percentage.zero())
				),
			() -> assertThat(response.getDividendChart()).isEmpty(),
			() -> assertThat(response.getSectorChart())
				.hasSize(1)
				.extracting(sector -> Tuple.tuple(sector.getSector(), sector.getSectorWeight()))
				.containsExactlyInAnyOrder(Tuple.tuple("현금", Percentage.zero()))
		);
	}

	@DisplayName("사용자는 포트폴리오에 실시간 상세 데이터를 조회한다")
	@Test
	void should_return_portfolio_stream_message_when_read_my_portfolio_stocks_in_real_time() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(member);

		BDDMockito.given(portfolioRepository.findByPortfolioIdWithAll(portfolio.getId()))
			.willReturn(Optional.of(portfolio));
		PortfolioDetailRealTimeItem portfolioDetailRealTimeItem = PortfolioDetailRealTimeItem.builder()
			.currentValuation(Money.won(720000L))
			.totalGain(Money.won(120_000L))
			.totalGainRate(Percentage.from(0.2))
			.dailyGain(Money.won(120_000L))
			.dailyGainRate(Percentage.from(0.2))
			.provisionalLossBalance(Money.zero())
			.build();
		BDDMockito.given(portfolioDetailFactory.createPortfolioDetailRealTimeItem(portfolio))
			.willReturn(portfolioDetailRealTimeItem);
		LocalDateTime dateAdded = LocalDate.of(2026, 7, 23).atStartOfDay();
		PortfolioHoldingRealTimeItem portfolioHoldingRealTimeItem1 = PortfolioHoldingRealTimeItem.builder()
			.id(1L)
			.currentValuation(Money.won(360_000L))
			.currentPrice(Money.won(60_000L))
			.dailyChange(Money.won(10_000L))
			.dailyChangeRate(Percentage.from(0.2))
			.totalGain(Money.won(60000L))
			.totalReturnRate(Percentage.from(0.2))
			.dateAdded(dateAdded)
			.build();
		PortfolioHoldingRealTimeItem portfolioHoldingRealTimeItem2 = PortfolioHoldingRealTimeItem.builder()
			.id(2L)
			.currentValuation(Money.won(360_000L))
			.currentPrice(Money.won(60_000L))
			.dailyChange(Money.won(10_000L))
			.dailyChangeRate(Percentage.from(0.2))
			.totalGain(Money.won(60000L))
			.totalReturnRate(Percentage.from(0.2))
			.dateAdded(dateAdded)
			.build();
		BDDMockito.given(
				portfolioHoldingDetailFactory.createPortfolioHoldingRealTimeItems(portfolio, portfolioCalculator))
			.willReturn(List.of(portfolioHoldingRealTimeItem1, portfolioHoldingRealTimeItem2));

		// when
		StreamMessage portfolioStreamMessage = service.getPortfolioReturns(portfolio.getId());

		// then
		assertAll(
			() -> assertThat(portfolioStreamMessage)
				.extracting(StreamMessage::getData)
				.asInstanceOf(type(PortfolioHoldingsRealTimeResponse.class))
				.extracting(PortfolioHoldingsRealTimeResponse::getPortfolioDetails)
				.extracting(
					PortfolioDetailRealTimeItem::getCurrentValuation,
					PortfolioDetailRealTimeItem::getTotalGain,
					PortfolioDetailRealTimeItem::getTotalGainRate,
					PortfolioDetailRealTimeItem::getDailyGain,
					PortfolioDetailRealTimeItem::getDailyGainRate,
					PortfolioDetailRealTimeItem::getProvisionalLossBalance
				)
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Money.won(720_000L),
					Money.won(120_000L),
					Percentage.from(0.2),
					Money.won(120_000L),
					Percentage.from(0.2),
					Money.zero()
				),

			() -> assertThat(portfolioStreamMessage)
				.extracting(StreamMessage::getData)
				.asInstanceOf(type(PortfolioHoldingsRealTimeResponse.class))
				.extracting(PortfolioHoldingsRealTimeResponse::getPortfolioHoldings)
				.asInstanceOf(list(PortfolioHoldingRealTimeItem.class))
				.extracting(item -> Tuple.tuple(
					item.getId(),
					item.getCurrentValuation(),
					item.getCurrentPrice(),
					item.getDailyChange(),
					item.getDailyChangeRate(),
					item.getTotalGain(),
					item.getTotalReturnRate(),
					item.getDateAdded()
				))
				.usingComparatorForType(Money::compareTo, Money.class)
				.usingComparatorForType(Percentage::compareTo, Percentage.class)
				.containsExactlyInAnyOrder(
					Tuple.tuple(
						1L,
						Money.won(360_000L),
						Money.won(60_000L),
						Money.won(10_000L),
						Percentage.from(0.2),
						Money.won(60000L),
						Percentage.from(0.2),
						LocalDate.of(2026, 7, 23).atStartOfDay()
					),
					Tuple.tuple(
						2L,
						Money.won(360000L),
						Money.won(60_000L),
						Money.won(10_000L),
						Percentage.from(0.2),
						Money.won(60000L),
						Percentage.from(0.2),
						LocalDate.of(2026, 7, 23).atStartOfDay()
					)
				)
		);
	}

	@DisplayName("사용자는 포트폴리오의 종목을 삭제한다")
	@Test
	void should_delete_portfolio_holding() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();
		PortfolioHolding portfolioHolding = PortfolioHolding.of(1L, portfolio, stock);

		BDDMockito.given(purchaseHistoryRepository.deleteAllByPortfolioHoldingIdIn(List.of(portfolioHolding.getId())))
			.willReturn(0);
		BDDMockito.given(portfolioHoldingRepository.deleteAllByIdIn(List.of(portfolioHolding.getId())))
			.willReturn(1);

		// when
		PortfolioStockDeleteResponse response = service.deletePortfolioStock(portfolioHolding.getId(),
			portfolio.getId());

		// then
		Assertions.assertThat(response)
			.extracting(PortfolioStockDeleteResponse::getPortfolioHoldingId)
			.isEqualTo(portfolioHolding.getId());
	}

	@DisplayName("사용자는 다수의 포트폴리오 종목을 삭제할 수 있다")
	@Test
	void should_delete_multiple_portfolio_holding_when_portfolio_holding_are_multiple_data() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock1 = TestDataFactory.createSamsungStock();
		Stock stock2 = TestDataFactory.createDongwhaPharmStock();
		PortfolioHolding portfolioHolding1 = TestDataFactory.createPortfolioHolding(1L, portfolio, stock1);
		PortfolioHolding portfolioHolding2 = TestDataFactory.createPortfolioHolding(2L, portfolio, stock2);

		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(5);
		Money purchasePerShare = Money.won(10000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory1 = TestDataFactory.createPurchaseHistory(1L, purchaseDate, numShares,
			purchasePerShare, memo, portfolioHolding1);
		PurchaseHistory purchaseHistory2 = TestDataFactory.createPurchaseHistory(2L, purchaseDate, numShares,
			purchasePerShare, memo, portfolioHolding2);
		portfolioHolding1.addPurchaseHistory(purchaseHistory1);
		portfolioHolding2.addPurchaseHistory(purchaseHistory2);

		List<Long> portfolioHoldingIds = List.of(portfolioHolding1.getId(), portfolioHolding2.getId());

		BDDMockito.given(portfolioHoldingRepository.existsById(portfolioHolding1.getId()))
			.willReturn(true);
		BDDMockito.given(portfolioHoldingRepository.existsById(portfolioHolding2.getId()))
			.willReturn(true);
		// when
		PortfolioStockDeletesResponse response = service.deletePortfolioHoldings(portfolio.getId(), member.getId(),
			portfolioHoldingIds);

		// then
		Assertions.assertThat(response)
			.extracting(PortfolioStockDeletesResponse::getPortfolioHoldingIds)
			.asInstanceOf(list(Long.class))
			.hasSize(2)
			.containsExactlyInAnyOrder(portfolioHolding1.getId(), portfolioHolding2.getId());
	}

	@DisplayName("사용자는 다수의 포트폴리오 삭제시 존재하지 않는 일부 포트폴리오 종목이 존재한다면 전부 삭제할 수 없다")
	@Test
	void should_throw_exception_when_portfolio_holding_is_not_exist() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock1 = TestDataFactory.createSamsungStock();
		PortfolioHolding portfolioHolding = TestDataFactory.createPortfolioHolding(1L, portfolio, stock1);
		LocalDateTime purchaseDate = LocalDateTime.of(2023, 9, 26, 9, 30, 0);
		Count numShares = Count.from(3);
		Money purchasePerShare = Money.won(50000);
		String memo = "첫구매";
		PurchaseHistory purchaseHistory = TestDataFactory.createPurchaseHistory(1L, purchaseDate, numShares,
			purchasePerShare, memo, portfolioHolding);
		portfolioHolding.addPurchaseHistory(purchaseHistory);

		Long notExistHoldingId = 9999L;
		List<Long> portfolioHoldingIds = List.of(portfolioHolding.getId(), notExistHoldingId);
		BDDMockito.given(portfolioHoldingRepository.existsById(portfolioHolding.getId()))
			.willReturn(true);
		BDDMockito.given(portfolioHoldingRepository.existsById(notExistHoldingId))
			.willReturn(false);

		// when
		Throwable throwable = catchThrowable(
			() -> service.deletePortfolioHoldings(portfolio.getId(), member.getId(), portfolioHoldingIds));

		// then
		assertThat(throwable)
			.isInstanceOf(HoldingNotFoundException.class)
			.hasMessage(notExistHoldingId.toString());
	}

	@DisplayName("사용자는 매입이력 없이 포트폴리오 종목을 추가할 수 있다")
	@Test
	void should_save_portfolio_holding_when_without_purchase_history_data() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock samsung = TestDataFactory.createSamsungStock();

		PortfolioHolding holding = PortfolioHolding.of(portfolio, samsung);
		BDDMockito.given(portfolioHoldingRepository.save(holding))
			.willReturn(PortfolioHolding.of(1L, portfolio, samsung));
		// when
		PortfolioHolding saveHolding = service.savePortfolioHolding(holding);
		// then
		Assertions.assertThat(saveHolding).isNotNull();
		Assertions.assertThat(saveHolding.getId()).isEqualTo(1L);
		Assertions.assertThat(saveHolding.getPurchaseHistories()).isEmpty();
	}

	@DisplayName("포트폴리오 종목이 없으면 빈 Optional을 반환한다")
	@Test
	void should_return_empty_list_when_not_registered_holding() {
		// given
		Member member = TestDataFactory.createMember(1L);
		Portfolio portfolio = TestDataFactory.createPortfolio(1L, member);
		Stock stock = TestDataFactory.createSamsungStock();

		BDDMockito.given(portfolioHoldingRepository.findByPortfolioAndStock(portfolio, stock))
			.willReturn(Optional.empty());
		// when
		Optional<PortfolioHolding> holding = service.getPortfolioHoldingBy(portfolio, stock);
		// then
		assertThat(holding).isEmpty();
	}

	// @DisplayName("포트폴리오 종목을 조회한다")
	// @Test
	// void getPortfolioHoldingBy_givenPortfolioAndStock_whenExistPortfolioHolding_thenReturnHoldingOptional() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	Portfolio portfolio = portfolioRepository.save(createPortfolio(member));
	// 	Stock stock = stockRepository.save(createSamsungStock());
	//
	// 	portfolioHoldingRepository.save(PortfolioHolding.of(portfolio, stock));
	// 	// when
	// 	Optional<PortfolioHolding> holding = service.getPortfolioHoldingBy(portfolio, stock);
	// 	// then
	// 	assertThat(holding).isPresent();
	// }
}
