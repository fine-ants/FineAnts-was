package co.fineants.api.domain.notification.service;

import static co.fineants.TestDataFactory.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class MarkNotificationsAsReadTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private MarkNotificationsAsRead markNotificationsAsRead;

	@DisplayName("사용자는 다른 사용자의 알림을 읽음 처리할 수 없다")
	@Test
	void markBy_whenOtherMemberRequest_thenThrowException() {
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));

		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> markNotificationsAsRead.markBy(hacker.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}
}
