package co.fineants.api.domain.portfolio.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.success.DashboardSuccessCode;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class DashboardRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private DashboardRestController controller;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	private MockMvc mockMvc;
	private Portfolio portfolio;

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		Member member = memberRepository.save(TestDataFactory.createMember());
		portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));
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
}
