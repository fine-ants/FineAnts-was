package co.fineants.api.domain.notification.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class DeleteNotificationsTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private DeleteNotifications deleteNotifications;
	private Member member;
	private List<Notification> notifications;

	@BeforeEach
	void setUp() {
		member = memberRepository.save(createMember());
		notifications = notificationRepository.saveAll(TestDataFactory.createNotifications(member));
		setAuthentication(member);
	}

	@DisplayName("사용자는 다른 사용자의 알림 메시지를 제거할 수 없습니다")
	@Test
	void deleteBy_whenOtherMemberDelete_thenThrowException() {
		// given
		Member hacker = memberRepository.save(createMember("hacker"));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> deleteNotifications.deleteBy(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}
}
