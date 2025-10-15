package co.fineants.member.application;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.fcm.domain.dto.response.FcmDeleteResponse;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.global.common.authorized.Authorized;
import co.fineants.api.global.common.authorized.service.MemberAuthorizedService;
import co.fineants.api.global.common.resource.ResourceId;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.presentation.dto.request.MemberNotificationPreferenceRequest;
import co.fineants.member.presentation.dto.response.MemberNotificationPreferenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationPreferenceService {

	private final MemberRepository memberRepository;
	private final FcmService fcmService;

	@Transactional
	public MemberNotificationPreferenceResponse registerDefaultNotificationPreference(Member member) {
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		member.setNotificationPreference(notificationPreference);
		return MemberNotificationPreferenceResponse.from(notificationPreference);
	}

	@Transactional
	@Authorized(serviceClass = MemberAuthorizedService.class)
	@Secured("ROLE_USER")
	public MemberNotificationPreferenceResponse updateNotificationPreference(
		@ResourceId Long memberId,
		MemberNotificationPreferenceRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
		NotificationPreference preference = request.toEntity();
		member.setNotificationPreference(preference);

		// 회원 계정의 전체 알림 설정이 모두 비활성화인 경우 FCM 토큰 삭제
		if (preference.isAllInActive() && request.hasFcmTokenId()) {
			FcmDeleteResponse response = fcmService.deleteToken(request.fcmTokenId());
			log.info("회원 알림 설정 전체 비활성화로 인한 결과 : {}", response);
		}
		return MemberNotificationPreferenceResponse.from(preference);
	}
}
