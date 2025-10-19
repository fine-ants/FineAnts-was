package co.fineants.member.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.MemberAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.domain.event.NotificationPreferenceChangeEvent;
import co.fineants.member.presentation.dto.request.MemberNotificationPreferenceRequest;
import co.fineants.member.presentation.dto.response.MemberNotificationPreferenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateNotificationPreference {

	private final MemberRepository memberRepository;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	@Authorized(serviceClass = MemberAuthorizedService.class)
	public MemberNotificationPreferenceResponse update(
		@ResourceId Long memberId,
		MemberNotificationPreferenceRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
		NotificationPreference preference = request.toEntity();
		member.setNotificationPreference(preference);

		eventPublisher.publishEvent(new NotificationPreferenceChangeEvent(memberId, request.fcmTokenId()));
		return MemberNotificationPreferenceResponse.from(preference);
	}
}
