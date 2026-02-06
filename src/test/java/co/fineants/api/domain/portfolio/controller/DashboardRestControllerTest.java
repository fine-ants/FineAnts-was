package co.fineants.api.domain.portfolio.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.gainhistory.repository.PortfolioGainHistoryRepository;
import co.fineants.api.domain.holding.repository.PortfolioHoldingRepository;
import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.success.DashboardSuccessCode;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.stock.domain.ActiveStockRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;

class DashboardRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private DashboardRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private PortfolioGainHistoryRepository portfolioGainHistoryRepository;

	@Autowired
	private ActiveStockRepository activeStockRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private PortfolioHoldingRepository portfolioHoldingRepository;

	@Autowired
	private CurrentPriceRepository currentPriceRepository;

	@Autowired
	private ClosingPriceRepository closingPriceRepository;

	private MockMvc mockMvc;
	private Portfolio portfolio;
	private Member member;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		member = memberRepository.save(TestDataFactory.createMember());
		portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
		PortfolioGainHistory history = PortfolioGainHistory.create(
			Money.won(1_000_000),
			Money.won(1_000_000),
			Money.won(1_000_000),
			Money.won(1_000_000),
			portfolio
		);
		portfolioGainHistoryRepository.save(history);
	}

	@DisplayName("사용자는 대시보드 개요를 조회한다")
	@Test
	void readOverview() throws Exception {
		// given

		// when & then
		mockMvc.perform(get("/api/dashboard/overview"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(DashboardSuccessCode.OK_OVERVIEW.getMessage())))
			.andExpect(jsonPath("data.username").value(equalTo("nemo1234")))
			.andExpect(jsonPath("data.totalValuation").value(equalTo(1000000)))
			.andExpect(jsonPath("data.totalInvestment").value(equalTo(0)))
			.andExpect(jsonPath("data.totalGain").value(equalTo(0)))
			.andExpect(jsonPath("data.totalGainRate").value(equalTo(0.0)))
			.andExpect(jsonPath("data.totalAnnualDividend").value(equalTo(0)))
			.andExpect(jsonPath("data.totalAnnualDividendYield").value(equalTo(0.0)));
	}

	@DisplayName("오버뷰 조회 - 활성 종목이 등록되어 있어야 한다")
	@Test
	void readOverview_ActiveStocks() throws Exception {
		// given
		Stock stock = stockRepository.save(createSamsungStock());
		portfolioHoldingRepository.save(createPortfolioHolding(portfolio, stock));

		currentPriceRepository.savePrice(stock.getTickerSymbol(), 50000L);
		closingPriceRepository.savePrice(stock.getTickerSymbol(), 48000L);

		// when
		mockMvc.perform(get("/api/dashboard/overview"))
			.andExpect(status().isOk());

		// then - 활성 종목 검증
		Awaitility.await()
			.atMost(Duration.ofSeconds(5))
			.untilAsserted(() -> Assertions.assertThat(activeStockRepository.size()).isEqualTo(1L));
	}

	@DisplayName("사용자는 파이차트를 조회한다")
	@Test
	void readPieChart() throws Exception {
		// given

		// when & then
		mockMvc.perform(get("/api/dashboard/pieChart"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(DashboardSuccessCode.OK_PORTFOLIO_PIE_CHART.getMessage())))
			.andExpect(jsonPath("data[0].id").value(portfolio.getId()))
			.andExpect(jsonPath("data[0].name").value("내꿈은 워렌버핏"))
			.andExpect(jsonPath("data[0].valuation").value(1000000))
			.andExpect(jsonPath("data[0].weight").value(100.0))
			.andExpect(jsonPath("data[0].totalGain").value(0))
			.andExpect(jsonPath("data[0].totalGainRate").value(0.0));
	}

	@DisplayName("사용자는 라인차트를 조회한다")
	@Test
	void readLineChart() throws Exception {
		// given

		// when & then
		mockMvc.perform(get("/api/dashboard/lineChart"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(DashboardSuccessCode.OK_LINE_CHART.getMessage())))
			.andExpect(jsonPath("data").isArray())
			.andExpect(jsonPath("data[0].time").value(notNullValue()))
			.andExpect(jsonPath("data[0].value").value(equalTo(2000000)));
	}
}
