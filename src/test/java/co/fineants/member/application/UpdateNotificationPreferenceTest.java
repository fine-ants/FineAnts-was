package co.fineants.member.application;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.presentation.dto.request.MemberNotificationPreferenceRequest;

class UpdateNotificationPreferenceTest extends AbstractContainerBaseTest {

	@Autowired
	private UpdateNotificationPreference service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@DisplayName("사용자는 계정의 알림 설정을 변경한다")
	@Test
	void updateNotificationPreference() {
		// given
		Member member = memberRepository.save(createMember());
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.build();

		setAuthentication(member);
		// when
		service.update(member.getId(), request);

		// then
		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		NotificationPreference expected = NotificationPreference.create(
			false,
			true,
			true,
			true);
		assertThat(findMember.getNotificationPreference()).isEqualTo(expected);
	}

	@DisplayName("사용자가 계정 알림 설정을 수정한다")
	@Test
	void updateNotificationPreference_whenNotExistPreference_thenRegisterPreference() {
		// given
		Member member = memberRepository.save(createMember());

		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.build();

		setAuthentication(member);
		// when
		service.update(member.getId(), request);

		// then
		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		NotificationPreference expected = NotificationPreference.create(
			false,
			false,
			false,
			false);
		assertThat(findMember.getNotificationPreference()).isEqualTo(expected);
	}

	@DisplayName("사용자는 회원 알림 설정 수정시 설정값을 모두 비활성화하는 경우 FcmToken을 제거하도록 합니다")
	@Test
	void updateNotificationPreference_whenPreferenceIsAllInActive_thenDeleteFcmToken() {
		// given
		Member member = memberRepository.save(createMember());
		FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.fcmTokenId(fcmToken.getId())
			.build();

		setAuthentication(member);
		// when
		service.update(member.getId(), request);

		// then
		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		NotificationPreference expected = NotificationPreference.create(
			false,
			false,
			false,
			false);
		assertThat(findMember.getNotificationPreference()).isEqualTo(expected);
	}

	@DisplayName("사용자는 푸시 알림을 허용하지 않은 상태(FCM 토큰 미등록 상태)에서 회원 알람 설정 수정시 FCM 토큰을 삭제하지 않는다")
	@Test
	void updateNotificationPreference_whenPreferenceIsAllInActiveAndFcmTokenIsNotStored_thenNotDeleteFcmToken() {
		// given
		Member member = memberRepository.save(createMember());
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.fcmTokenId(null)
			.build();

		setAuthentication(member);
		// when
		service.update(member.getId(), request);

		// then
		Member findMember = memberRepository.findById(member.getId()).orElseThrow();
		NotificationPreference expected = NotificationPreference.create(
			false,
			false,
			false,
			false
		);
		assertThat(findMember.getNotificationPreference()).isEqualTo(expected);
		assertThat(fcmRepository.findAllByMemberId(member.getId())).isEmpty();
	}

	@DisplayName("사용자는 다른 사용자의 회원 알림 설정을 수정할 수 없습니다")
	@Test
	void updateNotificationPreference_whenOtherMemberModify_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(true)
			.maxLossNotify(true)
			.targetPriceNotify(true)
			.build();

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> service.update(member.getId(), request));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(member.toString());
	}
}
