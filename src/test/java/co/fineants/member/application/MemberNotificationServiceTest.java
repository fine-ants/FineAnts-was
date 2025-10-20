package co.fineants.member.application;

import static co.fineants.api.domain.notification.domain.entity.type.NotificationType.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.api.global.errors.exception.business.NotificationNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

class MemberNotificationServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberNotificationService notificationService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	private List<Notification> createNotifications(Member member) {
		return List.of(
			Notification.stockTargetPriceNotification(
				"종목 지정가", "005930", "/stock/005930", member, List.of("messageId"), "삼성전자일반주",
				Money.won(60000L),
				1L
			),
			Notification.portfolioNotification(
				"포트폴리오", PORTFOLIO_TARGET_GAIN, "1", "/portfolio/1", member, List.of("messageId"), "포트폴리오1",
				1L
			),
			Notification.portfolioNotification(
				"포트폴리오", PORTFOLIO_MAX_LOSS, "2", "/portfolio/1", member, List.of("messageId"), "포트폴리오2",
				2L
			)
		);
	}

	@DisplayName("사용자는 알림 모두 읽습니다")
	@Test
	void fetchMemberNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(member);
		// when
		List<Long> readNotificationIds = notificationService.fetchMemberNotifications(member.getId(), notificationIds);

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
	void fetchMemberNotifications_whenNotExistNotificationIds_thenThrow404Error() {
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
			() -> notificationService.fetchMemberNotifications(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotificationNotFoundException.class)
			.hasMessage(notificationIds.toString());
	}

	@DisplayName("사용자는 다른 사용자의 알림을 읽음 처리할 수 없다")
	@Test
	void fetchMemberNotifications_whenOtherMemberRequest_thenThrowException() {
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));

		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> notificationService.fetchMemberNotifications(hacker.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}

	@DisplayName("사용자는 알림을 전체 삭제합니다")
	@Test
	void deleteAllNotifications() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(member);
		// when
		List<Long> deletedAllNotifications = notificationService.deleteMemberNotifications(member.getId(),
			notificationIds);

		// then
		assertThat(deletedAllNotifications).hasSize(3);
		assertThat(notificationRepository.findAllByMemberIdAndIds(member.getId(), notificationIds)).isEmpty();
	}

	@DisplayName("사용자는 존재하지 않은 알람들을 삭제할 수 없습니다")
	@Test
	void deleteAllNotifications_whenNotExistNotificationId_then404Error() {
		// given
		Member member = memberRepository.save(createMember());
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.collect(Collectors.toList());
		notificationIds.add(9999L);

		setAuthentication(member);
		// when
		Throwable throwable = catchThrowable(() -> notificationService.deleteMemberNotifications(member.getId(),
			notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(NotificationNotFoundException.class)
			.hasMessage(notificationIds.toString());
	}

	@DisplayName("사용자는 다른 사용자의 알림 메시지를 제거할 수 없습니다")
	@Test
	void deleteAllNotifications_whenOtherMemberDelete_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		List<Notification> notifications = notificationRepository.saveAll(createNotifications(member));
		List<Long> notificationIds = notifications.stream()
			.map(Notification::getId)
			.toList();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(
			() -> notificationService.deleteMemberNotifications(member.getId(), notificationIds));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}
}
