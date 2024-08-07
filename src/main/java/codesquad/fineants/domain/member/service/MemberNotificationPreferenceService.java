package codesquad.fineants.domain.member.service;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.fcm.domain.dto.response.FcmDeleteResponse;
import codesquad.fineants.domain.fcm.service.FcmService;
import codesquad.fineants.domain.member.domain.dto.request.MemberNotificationPreferenceRequest;
import codesquad.fineants.domain.member.domain.dto.response.MemberNotificationPreferenceResponse;
import codesquad.fineants.domain.member.domain.entity.Member;
import codesquad.fineants.domain.member.repository.MemberRepository;
import codesquad.fineants.domain.notificationpreference.domain.entity.NotificationPreference;
import codesquad.fineants.domain.notificationpreference.repository.NotificationPreferenceRepository;
import codesquad.fineants.global.common.authorized.Authorized;
import codesquad.fineants.global.common.authorized.service.MemberNotificationPreferenceAuthorizedService;
import codesquad.fineants.global.common.resource.ResourceId;
import codesquad.fineants.global.errors.errorcode.MemberErrorCode;
import codesquad.fineants.global.errors.errorcode.NotificationPreferenceErrorCode;
import codesquad.fineants.global.errors.exception.FineAntsException;
import codesquad.fineants.global.errors.exception.NotFoundResourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberNotificationPreferenceService {

	private final NotificationPreferenceRepository notificationPreferenceRepository;
	private final MemberRepository memberRepository;
	private final FcmService fcmService;

	@Transactional
	public MemberNotificationPreferenceResponse registerDefaultNotificationPreference(Member member) {
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(member.getId())
			.orElseGet(() -> NotificationPreference.defaultSetting(member));
		NotificationPreference saveNotificationPreference = notificationPreferenceRepository.save(preference);
		return MemberNotificationPreferenceResponse.from(saveNotificationPreference);
	}

	@Transactional
	@Authorized(serviceClass = MemberNotificationPreferenceAuthorizedService.class)
	@Secured("ROLE_USER")
	public MemberNotificationPreferenceResponse updateNotificationPreference(
		@ResourceId Long memberId,
		MemberNotificationPreferenceRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new FineAntsException(MemberErrorCode.NOT_FOUND_MEMBER));
		notificationPreferenceRepository.findByMemberId(memberId)
			.ifPresentOrElse(
				notificationPreference -> notificationPreference.changePreference(request.toEntity(member)),
				() -> notificationPreferenceRepository.save(NotificationPreference.defaultSetting(member)));
		NotificationPreference preference = notificationPreferenceRepository.findByMemberId(memberId)
			.orElseThrow(() ->
				new NotFoundResourceException(NotificationPreferenceErrorCode.NOT_FOUND_NOTIFICATION_PREFERENCE));

		// 회원 계정의 전체 알림 설정이 모두 비활성화인 경우 FCM 토큰 삭제
		if (preference.isAllInActive() && request.getFcmTokenId() != null) {
			FcmDeleteResponse response = fcmService.deleteToken(request.getFcmTokenId());
			log.info("회원 알림 설정 전체 비활성화로 인한 결과 : {}", response);
		}
		return MemberNotificationPreferenceResponse.from(preference);
	}
}
