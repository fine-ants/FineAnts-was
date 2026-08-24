package co.fineants.api.domain.notification.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.NotificationNotFoundException;
import co.fineants.member.domain.Member;

@ExtendWith(MockitoExtension.class)
class DeleteNotificationsUnitTest {

	@InjectMocks
	private DeleteNotifications deleteNotifications;

	@Mock
	private NotificationRepository notificationRepository;

	private Member member;
	private List<Notification> notifications;

	@BeforeEach
	void setUp() {
		member = TestDataFactory.createMember(1L);
		notifications = TestDataFactory.createNotifications(member);
	}

	@DisplayName("사용자는 알림을 전체 삭제합니다")
	@Test
	void should_delete_notification_data() {
		// given
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();
		BDDMockito.given(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds))
			.willReturn(notifications);

		// when
		List<Long> deletedAllNotifications = deleteNotifications.deleteBy(member.getId(),
			notificationIds);

		// then
		assertThat(deletedAllNotifications).hasSize(3);
		BDDMockito.verify(notificationRepository, Mockito.times(1))
			.deleteAllById(notificationIds);
	}

	@DisplayName("사용자는 존재하지 않은 알람들을 삭제할 수 없습니다")
	@Test
	void should_throw_exception_when_not_exist_notification_ids() {
		// given
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());
		notificationIds.add(9999L);

		// when
		Throwable throwable = catchThrowable(() -> deleteNotifications.deleteBy(member.getId(),
			notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotificationNotFoundException.class)
			.hasMessage(notificationIds.toString());
	}
}
