package co.fineants.member.application.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import co.fineants.api.domain.fcm.domain.dto.response.FcmDeleteResponse;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.global.errors.exception.business.MemberNotFoundException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.domain.event.NotificationPreferenceChangeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceChangeEventListener {

	private final MemberRepository memberRepository;
	private final FcmService fcmService;

	// 회원 계정의 전체 알림 설정이 모두 비활성화인 경우 FCM 토큰 삭제
	@TransactionalEventListener
	@Async
	public void on(NotificationPreferenceChangeEvent event) {
		Member member = memberRepository.findById(event.getMemberId())
			.orElseThrow(() -> new MemberNotFoundException(event.getMemberId().toString()));
		NotificationPreference preference = member.getNotificationPreference();
		Long fcmTokenId = event.getFcmTokenId();
		if (preference.isAllInActive() && fcmTokenId != null) {
			FcmDeleteResponse response = fcmService.deleteToken(fcmTokenId);
			log.info("회원 알림 설정 전체 비활성화로 인한 결과 : {}", response);
		}
	}
}
