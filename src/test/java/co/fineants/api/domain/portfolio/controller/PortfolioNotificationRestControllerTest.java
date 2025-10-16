package co.fineants.api.domain.portfolio.controller;

import static co.fineants.api.global.success.PortfolioSuccessCode.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.portfolio.domain.dto.request.PortfolioNotificationUpdateRequest;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class PortfolioNotificationRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private PortfolioNotificationRestController controller;

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

	@DisplayName("사용자는 포트폴리오의 목표수익금액 알람을 활성화합니다.")
	@Test
	void modifyNotificationTargetGain() throws Exception {
		// given
		PortfolioNotificationUpdateRequest request = new PortfolioNotificationUpdateRequest(true);

		// when & then
		mockMvc.perform(put("/api/portfolio/{portfolioId}/notification/targetGain", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(
				jsonPath("message").value(equalTo(OK_MODIFY_PORTFOLIO_TARGET_GAIN_ACTIVE_NOTIFICATION.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 포트폴리오의 목표수익금액 알람을 비활성화합니다.")
	@Test
	void modifyNotificationTargetGainWithInActive() throws Exception {
		// given
		PortfolioNotificationUpdateRequest request = new PortfolioNotificationUpdateRequest(false);

		// when & then
		mockMvc.perform(put("/api/portfolio/{portfolioId}/notification/targetGain", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(
				equalTo(OK_MODIFY_PORTFOLIO_TARGET_GAIN_INACTIVE_NOTIFICATION.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액 알람을 활성화합니다.")
	@Test
	void modifyNotificationMaximumLoss() throws Exception {
		// given
		PortfolioNotificationUpdateRequest request = new PortfolioNotificationUpdateRequest(true);

		// when & then
		mockMvc.perform(put("/api/portfolio/{portfolioId}/notification/maxLoss", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(
				equalTo(OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_ACTIVE_NOTIFICATION.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 포트폴리오의 최대손실금액 알람을 비활성화합니다.")
	@Test
	void modifyNotificationMaximumLossWithInActive() throws Exception {
		// given
		PortfolioNotificationUpdateRequest request = new PortfolioNotificationUpdateRequest(false);

		// when & then
		mockMvc.perform(put("/api/portfolio/{portfolioId}/notification/maxLoss", portfolio.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(StandardCharsets.UTF_8)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(
				equalTo(OK_MODIFY_PORTFOLIO_MAXIMUM_LOSS_INACTIVE_NOTIFICATION.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}
}
