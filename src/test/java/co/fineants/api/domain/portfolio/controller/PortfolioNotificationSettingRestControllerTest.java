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
import co.fineants.api.global.success.PortfolioSuccessCode;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class PortfolioNotificationSettingRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioNotificationSettingRestController controller;

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

	@DisplayName("사용자는 포트폴리오 활성 알림 목록을 조회합니다")
	@Test
	void searchPortfolioNotificationSetting() throws Exception {
		// given

		// when & then
		mockMvc.perform(get("/api/portfolios/notification/settings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(
				equalTo(PortfolioSuccessCode.OK_SEARCH_PORTFOLIO_NOTIFICATION_SETTINGS.getMessage())))
			.andExpect(jsonPath("data.portfolios[0].portfolioId").value(equalTo(portfolio.getId().intValue())))
			.andExpect(jsonPath("data.portfolios[0].securitiesFirm").value(equalTo("토스증권")))
			.andExpect(jsonPath("data.portfolios[0].name").value(equalTo("내꿈은 워렌버핏")))
			.andExpect(jsonPath("data.portfolios[0].targetGainNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].maxLossNotify").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].isTargetGainSet").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].isMaxLossSet").value(equalTo(true)))
			.andExpect(jsonPath("data.portfolios[0].createdAt").value(notNullValue()));
	}
}
