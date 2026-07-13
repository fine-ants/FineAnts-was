package co.fineants.member.application;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateNotificationPreference {

	private final MemberRepository memberRepository;
	private final FcmService fcmService;

	@Transactional
	@Authorized(serviceClass = MemberAuthorizedService.class)
	public void update(
		@ResourceId Long memberId,
		MemberNotificationPreferenceRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
		NotificationPreference preference = request.toEntity();
		member.setNotificationPreference(preference);

		deleteFcmToken(preference, request.fcmTokenId());
	}

	// 계정 알림 설정이 모두 비활성화인 경우 토큰을 삭제하는 기능
	private void deleteFcmToken(NotificationPreference preference, Long fcmTokenId) {
		if (preference.isAllInActive() && fcmTokenId != null) {
			FcmDeleteResponse response = fcmService.deleteToken(fcmTokenId);
			log.info("회원 알림 설정 전체 비활성화로 인한 결과 : {}", response);
		}
	}
}
