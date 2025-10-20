package co.fineants.api.domain.notification.service;

import static co.fineants.TestDataFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.api.global.errors.exception.business.NotificationNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class MarkNotificationsAsReadTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private MarkNotificationsAsRead markNotificationsAsRead;

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void markBy() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(member);
		// when
		List<Long> readNotificationIds = markNotificationsAsRead.markBy(member.getId(), notificationIds);

		// then
		assertAll(
			() -> assertThat(readNotificationIds)
				.hasSize(3)
				.containsAll(notificationIds),
			() -> assertThat(notificationRepository.findAllById(Objects.requireNonNull(readNotificationIds))
				.stream()
				.allMatch(Notification::getIsRead))
				.isTrue()
		);
	}

	@DisplayName("사용자는 존재하지 않는 알람을 읽음 처리할 수 없다")
	@Test
	void markBy_whenNotExistNotificationIds_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());

		Long notExistNotificationId = 9999L;
		notificationIds.add(notExistNotificationId);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(
			() -> markNotificationsAsRead.markBy(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotificationNotFoundException.class)
			.hasMessage(notificationIds.toString());
	}

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
