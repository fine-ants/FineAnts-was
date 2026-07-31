package co.fineants.api.domain.notification.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.member.domain.Member;

@ExtendWith(MockitoExtension.class)
class MarkNotificationsAsReadUnitTest {
	@InjectMocks
	private MarkNotificationsAsRead markNotificationsAsRead;

	@Mock
	private NotificationRepository notificationRepository;

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void should_is_read_data_is_true_when_mark_notification() {
		// given
		Member member = TestDataFactory.createMember(1L);
		List<Notification> notifications = TestDataFactory.createNotifications(member);
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		BDDMockito.given(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds))
			.willReturn(notifications);
		// when
		List<Long> readNotificationIds = markNotificationsAsRead.markBy(member.getId(), notificationIds);

		// then
		assertThat(readNotificationIds)
			.hasSize(3)
			.containsAll(notificationIds);
		assertThat(notifications)
			.allMatch(Notification::getIsRead);
	}

	// @DisplayName("사용자는 존재하지 않는 알람을 읽음 처리할 수 없다")
	// @Test
	// void markBy_whenNotExistNotificationIds_thenThrowException() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
	// 	List<Long> notificationIds = notifications.stream()
	// 		.map(Notification::getId)
	// 		.collect(Collectors.toList());
	//
	// 	Long notExistNotificationId = 9999L;
	// 	notificationIds.add(notExistNotificationId);
	//
	// 	setAuthentication(member);
	// 	// when
	// 	Throwable throwable = catchThrowable(
	// 		() -> markNotificationsAsRead.markBy(member.getId(), notificationIds));
	//
	// 	// then
	// 	assertThat(throwable)
	// 		.isInstanceOf(NotificationNotFoundException.class)
	// 		.hasMessage(notificationIds.toString());
	// }
	//
	// @DisplayName("사용자는 다른 사용자의 알림을 읽음 처리할 수 없다")
	// @Test
	// void markBy_whenOtherMemberRequest_thenThrowException() {
	// 	Member member = memberRepository.save(createMember());
	// 	Member hacker = memberRepository.save(createMember("hacker"));
	//
	// 	List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
	// 	List<Long> notificationIds = notifications.stream()
	// 		.map(Notification::getId)
	// 		.toList();
	//
	// 	setAuthentication(hacker);
	// 	// when
	// 	Throwable throwable = catchThrowable(
	// 		() -> markNotificationsAsRead.markBy(hacker.getId(), notificationIds));
	//
	// 	// then
	// 	assertThat(throwable)
	// 		.isInstanceOf(ForbiddenException.class);
	// }
}
