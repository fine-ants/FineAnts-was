package co.fineants.member.presentation;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
import co.fineants.member.presentation.dto.request.MemberNotificationAllDeleteRequest;
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
		Member member = memberRepository.save(TestDataFactory.createMember());
		List<Long> notificationIds = Collections.emptyList();
		MemberNotificationAllReadRequest request = new MemberNotificationAllReadRequest(notificationIds);
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("notificationIds")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("읽을 알림의 개수는 1개 이상이어야 합니다")));
	}

	@DisplayName("사용자는 유효하지 않은 입력으로 알림을 읽을 수 없습니다")
	@Test
	void readAllNotifications_whenInvalidInput_thenResponse400Error() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		MemberNotificationAllReadRequest request = new MemberNotificationAllReadRequest(null);
		// when & then
		mockMvc.perform(patch("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.BAD_REQUEST.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.BAD_REQUEST.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo("잘못된 입력형식입니다")))
			.andExpect(jsonPath("data[0].field").value(equalTo("notificationIds")))
			.andExpect(jsonPath("data[0].defaultMessage").value(equalTo("필수 정보입니다")));
	}

	@DisplayName("사용자는 알람을 모두 삭제합니다")
	@Test
	void deleteAllNotifications() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		Notification notification1 = notificationRepository.save(
			TestDataFactory.createPortfolioNotification(member, portfolio, PORTFOLIO_MAX_LOSS));
		Notification notification2 = notificationRepository.save(
			TestDataFactory.createPortfolioNotification(member, portfolio, PORTFOLIO_TARGET_GAIN));

		List<Long> notificationIds = List.of(notification1.getId(), notification2.getId());
		MemberNotificationAllDeleteRequest request = new MemberNotificationAllDeleteRequest(notificationIds);
		// when & then
		mockMvc.perform(delete("/api/members/{memberId}/notifications", member.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(ObjectMapperUtil.serialize(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(MemberSuccessCode.OK_DELETED_ALL_NOTIFICATIONS.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}

	@DisplayName("사용자는 특정 알람을 삭제합니다")
	@Test
	void deleteNotification() throws Exception {
		// given
		Member member = memberRepository.save(TestDataFactory.createMember());
		Portfolio portfolio = portfolioRepository.save(TestDataFactory.createPortfolio(member));

		Notification notification = notificationRepository.save(
			TestDataFactory.createPortfolioNotification(member, portfolio, PORTFOLIO_MAX_LOSS));
		// when & then
		mockMvc.perform(delete("/api/members/{memberId}/notifications/{notificationId}",
				member.getId(), notification.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(HttpStatus.OK.value())))
			.andExpect(jsonPath("status").value(equalTo(HttpStatus.OK.getReasonPhrase())))
			.andExpect(jsonPath("message").value(equalTo(MemberSuccessCode.OK_DELETED_NOTIFICATION.getMessage())))
			.andExpect(jsonPath("data").value(nullValue()));
	}
}
