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

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
		Member member = memberRepository.save(TestDataFactory.createMember());
		portfolioRepository.save(TestDataFactory.createPortfolio(member));
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
}
