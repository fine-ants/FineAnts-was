package co.fineants.api.domain.notification.service;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.domain.entity.NotificationBody;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.presentation.dto.response.ListNotificationResponse;
import co.fineants.member.presentation.dto.response.NotificationDto;

class ListNotificationsTest extends AbstractContainerBaseTest {

	@Autowired
	private ListNotifications listNotifications;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;
	private Member member;

	@BeforeEach
	void setUp() {
		member = memberRepository.save(createMember());
		setAuthentication(member);
	}

	@DisplayName("사용자는 회원 알림 목록을 조회합니다")
	@Test
	void byId() {
		// given
		List<Notification> notifications = notificationRepository.saveAll(TestDataFactory.createNotifications(member));
		// when
		ListNotificationResponse response = listNotifications.byId(member.getId());

		// then
		assertThat(response)
			.extracting(ListNotificationResponse::getNotifications)
			.asList()
			.hasSize(3)
			.containsExactly(
				NotificationDto.builder()
					.notificationId(notifications.get(2).getId())
					.title("포트폴리오")
					.body(NotificationBody.portfolio("포트폴리오2", PORTFOLIO_MAX_LOSS))
					.timestamp(LocalDateTime.of(2024, 1, 24, 10, 10, 10))
					.isRead(false)
					.type(PORTFOLIO_MAX_LOSS.getCategory())
					.referenceId(notifications.get(2).getReferenceId())
					.build(),
				NotificationDto.builder()
					.notificationId(notifications.get(1).getId())
					.title("포트폴리오")
					.body(NotificationBody.portfolio("포트폴리오1", PORTFOLIO_TARGET_GAIN))
					.timestamp(LocalDateTime.of(2024, 1, 23, 10, 10, 10))
					.isRead(false)
					.type(PORTFOLIO_TARGET_GAIN.getCategory())
					.referenceId(notifications.get(1).getReferenceId())
					.build(),
				NotificationDto.builder()
					.notificationId(notifications.get(0).getId())
					.title("지정가")
					.body(NotificationBody.stock("삼성전자", Money.won(60000L)))
					.timestamp(LocalDateTime.of(2024, 1, 22, 10, 10, 10))
					.isRead(true)
					.type(STOCK_TARGET_PRICE.getCategory())
					.referenceId(notifications.get(0).getReferenceId())
					.build()
			);
	}

	@DisplayName("사용자는 다른 사용자의 알림 메시지들을 조회할 수 없습니다.")
	@Test
	void byId_whenOtherMemberFetch_thenThrowException() {
		// given
		Member hacker = memberRepository.save(createMember("hacker"));
		notificationRepository.saveAll(TestDataFactory.createNotifications(member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> listNotifications.byId(member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}

}
