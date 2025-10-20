package co.fineants.api.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.notification.repository.NotificationRepository;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.NotificationAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.member.presentation.dto.response.ListNotificationResponse;
import co.fineants.member.presentation.dto.response.MemberNotification;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListNotifications {

	private final NotificationRepository repository;

	@Transactional(readOnly = true)
	@Authorized(serviceClass = NotificationAuthorizedService.class)
	public ListNotificationResponse listNotifications(@ResourceId Long memberId) {
		List<MemberNotification> notifications = repository.findAllByMemberId(memberId).stream()
			.map(MemberNotification::from)
			.toList();
		return new ListNotificationResponse(notifications);
	}
}
