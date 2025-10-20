package co.fineants.member.application;

import java.util.List;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.NotificationAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.exception.business.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationService {

	private final NotificationRepository notificationRepository;

	// 입력 받은 알림들 중에서 안 읽은 알람들을 읽음 처리하고 읽은 알림의 등록번호 리스트를 반환
	@Transactional
	@Authorized(serviceClass = NotificationAuthorizedService.class)
	@Secured("ROLE_USER")
	public List<Long> fetchMemberNotifications(@ResourceId Long memberId, List<Long> notificationIds) {
		verifyExistNotifications(memberId, notificationIds);

		// 읽지 않은 알림 조회
		List<Notification> notifications = notificationRepository.findAllByMemberIdAndIds(memberId, notificationIds)
			.stream()
			.filter(notification -> !notification.getIsRead())
			.toList();
		log.info("읽지 않은 알림 목록 개수 : {}개", notifications.size());

		// 알림 읽기 처리
		notifications.forEach(Notification::read);

		// 읽은 알림들의 등록번호 반환
		return notifications.stream()
			.map(Notification::getId)
			.toList();
	}

	private void verifyExistNotifications(Long memberId, List<Long> notificationIds) {
		List<Notification> findNotifications = notificationRepository.findAllByMemberIdAndIds(memberId,
			notificationIds);
		if (notificationIds.size() != findNotifications.size()) {
			throw new NotificationNotFoundException(notificationIds.toString());
		}
	}

	@Transactional
	@Authorized(serviceClass = NotificationAuthorizedService.class)
	@Secured("ROLE_USER")
	public List<Long> deleteMemberNotifications(@ResourceId Long memberId, List<Long> notificationIds) {
		verifyExistNotifications(memberId, notificationIds);

		// 알림 삭제 처리
		notificationRepository.deleteAllById(notificationIds);

		// 삭제한 알림들의 등록번호를 반환
		return notificationIds;
	}
}
