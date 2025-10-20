package co.fineants.api.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.NotificationAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.exception.business.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteNotifications {

	private final NotificationRepository notificationRepository;

	@Transactional
	@Authorized(serviceClass = NotificationAuthorizedService.class)
	public List<Long> deleteBy(@ResourceId Long memberId, List<Long> notificationIds) {
		verifyExistNotifications(memberId, notificationIds);

		// 알림 삭제 처리
		notificationRepository.deleteAllById(notificationIds);

		// 삭제한 알림들의 등록번호를 반환
		return notificationIds;
	}

	private void verifyExistNotifications(Long memberId, List<Long> notificationIds) {
		List<Notification> findNotifications = notificationRepository.findAllByMemberIdAndIds(memberId,
			notificationIds);
		if (notificationIds.size() != findNotifications.size()) {
			throw new NotificationNotFoundException(notificationIds.toString());
		}
	}
}
