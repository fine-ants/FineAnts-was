package co.fineants.member.presentation;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.NotificationBody;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.portfolio.repository.PortfolioRepository;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.global.util.ObjectMapperUtil;
import co.fineants.member.application.MemberNotificationService;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.request.MemberNotificationAllReadRequest;
import co.fineants.member.presentation.dto.response.MemberNotification;

class MemberNotificationRestControllerTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberNotificationService memberNotificationService;

	@Autowired
	private MemberNotificationRestController controller;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PortfolioRepository portfolioRepository;

	private MockMvc mockMvc;

	private List<MemberNotification> createNotifications() {
		return List.of(MemberNotification.builder()
				.notificationId(3L)
				.title("포트폴리오")
				.body(NotificationBody.portfolio("포트폴리오2", PORTFOLIO_MAX_LOSS))
				.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
				.isRead(false)
				.type(PORTFOLIO_MAX_LOSS.getCategory())
				.referenceId("2")
				.build(),
			MemberNotification.builder()
				.notificationId(2L)
				.title("포트폴리오")
				.body(NotificationBody.portfolio("포트폴리오1", PORTFOLIO_TARGET_GAIN))
				.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
				.isRead(false)
				.type(PORTFOLIO_TARGET_GAIN.getCategory())
				.referenceId("1")
				.build(),
			MemberNotification.builder()
				.notificationId(1L)
				.title("지정가")
				.body(NotificationBody.stock("삼성전자", Money.won(60000L)))
				.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
				.isRead(true)
				.type(STOCK_TARGET_PRICE.getCategory())
				.referenceId("005930")
				.build());
	}

	@BeforeEach
	void setUp() {
		mockMvc = createMockMvc(controller);
	}

	@DisplayName("사용자는 알림 목록 조회합니다")
	@Test
	void fetchNotifications() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		Notification notification = notificationRepository.save(
			TestDataFactory.createPortfolioNotification(member, portfolio, PORTFOLIO_MAX_LOSS));
		String title = "포트폴리오";
		String portfolioName = portfolio.name();
		String notificationType = PORTFOLIO_MAX_LOSS.getName();
		String type = PORTFOLIO_MAX_LOSS.getCategory();
		String referenceId = portfolio.getReferenceId();

		// when & then
		mockMvc.perform(get("/api/members/{memberId}/notifications", member.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(MemberSuccessCode.OK_READ_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data.notifications").isArray())
			.andExpect(jsonPath("data.notifications", hasSize(1)))
			.andExpect(jsonPath("data.notifications[0].notificationId").value(equalTo(notification.getId().intValue())))
			.andExpect(jsonPath("data.notifications[0].title").value(equalTo(title)))
			.andExpect(jsonPath("data.notifications[0].body.name").value(equalTo(portfolioName)))
			.andExpect(jsonPath("data.notifications[0].body.target").value(equalTo(notificationType)))
			.andExpect(jsonPath("data.notifications[0].timestamp").value(notNullValue()))
			.andExpect(jsonPath("data.notifications[0].isRead").value(equalTo(false)))
			.andExpect(jsonPath("data.notifications[0].type").value(equalTo(type)))
			.andExpect(jsonPath("data.notifications[0].referenceId").value(equalTo(referenceId)));
	}

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void readAllNotifications() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		Notification notification1 = notificationRepository.save(
			TestDataFactory.createPortfolioNotification(member, portfolio, PORTFOLIO_MAX_LOSS));
		Notification notification2 = notificationRepository.save(
			TestDataFactory.createPortfolioNotification(member, portfolio, PORTFOLIO_TARGET_GAIN));

		List<Long> notificationIds = List.of(notification1.getId(), notification2.getId());
		MemberNotificationAllReadRequest request = new MemberNotificationAllReadRequest(notificationIds);
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(MemberSuccessCode.OK_FETCH_ALL_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 빈 리스트를 전달하여 알림을 읽을 수 없습니다")
	@Test
	void readAllNotifications_whenEmptyList_thenResponse400Error() throws Exception {
		// given
		Long memberId = 1L;

		List<MemberNotification> mockNotifications = createNotifications();
		given(memberNotificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(
				List.of(
					mockNotifications.get(0).getNotificationId(),
					mockNotifications.get(1).getNotificationId()
				)
			);

		List<Long> notificationIds = Collections.emptyList();
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(Map.of("notificationIds", notificationIds))))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 알림을 읽을 수 없습니다")
	@Test
	void readAllNotifications_whenInvalidInput_thenResponse400Error() throws Exception {
		// given
		Long memberId = 1L;

		List<MemberNotification> mockNotifications = createNotifications();
		given(memberNotificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(
				List.of(
					mockNotifications.get(0).getNotificationId(),
					mockNotifications.get(1).getNotificationId()
				)
			);

		Map<String, Object> requestBodyMap = new HashMap<>();
		requestBodyMap.put("notificationIds", null);
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(requestBodyMap)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(400)))
			.andExpect(jsonPath("status").value(equalTo("Bad Request")))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")));
	}

	@DisplayName("사용자는 알람을 모두 삭제합니다")
	@Test
	void deleteAllNotifications() throws Exception {
		// given
		Long memberId = 1L;

		List<MemberNotification> mockNotifications = createNotifications();
		List<Long> notificationIds = mockNotifications.stream()
			.map(MemberNotification::getNotificationId)
			.toList();
		given(memberNotificationService.fetchMemberNotifications(anyLong(), anyList()))
			.willReturn(notificationIds);

		// when & then
		mockMvc.perform(delete("/api/members/{memberId}/notifications",
				memberId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(Map.of("notificationIds", notificationIds))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 전체 삭제를 성공하였습니다")));
	}

	@DisplayName("사용자는 특정 알람을 삭제합니다")
	@Test
	void deleteNotification() throws Exception {
		// given
		Long memberId = 1L;

		MemberNotification mockNotification = MemberNotification.builder()
			.notificationId(3L)
			.title("포트폴리오")
			.body(NotificationBody.portfolio("포트폴리오2", PORTFOLIO_MAX_LOSS))
			.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
			.isRead(false)
			.type(PORTFOLIO_MAX_LOSS.getCategory())
			.referenceId("2")
			.build();
		given(memberNotificationService.deleteMemberNotifications(anyLong(), anyList()))
			.willReturn(List.of(mockNotification.getNotificationId()));

		// when & then
		mockMvc.perform(delete("/api/members/{memberId}/notifications/{notificationId}", memberId,
				mockNotification.getNotificationId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("알림 삭제를 성공하였습니다")));
	}
}
