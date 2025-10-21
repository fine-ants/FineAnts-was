package co.fineants.api.domain.notification.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.api.global.errors.exception.business.NotificationNotFoundException;
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

	@DisplayName("사용자는 알림을 전체 삭제합니다")
	@Test
	void deleteBy() {
		// given
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		// when
		List<Long> deletedAllNotifications = deleteNotifications.deleteBy(member.getId(),
			notificationIds);

		// then
		assertThat(deletedAllNotifications).hasSize(3);
		assertThat(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds)).isEmpty();
	}

	@DisplayName("사용자는 존재하지 않은 알람들을 삭제할 수 없습니다")
	@Test
	void deleteBy_whenNotExistNotificationId_thenThrowException() {
		// given
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());
		notificationIds.add(9999L);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> deleteNotifications.deleteBy(member.getId(),
			notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotificationNotFoundException.class)
			.hasMessage(notificationIds.toString());
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
