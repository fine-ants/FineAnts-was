package co.fineants.api.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.member.domain.dto.request.MemberNotificationPreferenceRequest;
import co.fineants.api.domain.member.domain.dto.response.MemberNotificationPreferenceResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.domain.notificationpreference.domain.entity.NotificationPreference;
import co.fineants.api.domain.notificationpreference.repository.NotificationPreferenceRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;

class MemberNotificationPreferenceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private MemberNotificationPreferenceService service;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private NotificationPreferenceRepository repository;

	@Autowired
	private FcmRepository fcmRepository;

	@DisplayName("사용자는 계정 알림 설정을 등록합니다")
	@Test
	void registerDefaultNotificationPreference() {
		// given
		Member member = memberRepository.save(createMember());
		NotificationPreference preference = member.getNotificationPreference();
		preference.changePreference(createNotificationPreference(false, false, false, false));
		repository.save(preference);

		// when
		MemberNotificationPreferenceResponse response = service.registerDefaultNotificationPreference(member);

		// then
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(repository.findByMemberId(member.getId())).isPresent()
		);
	}

	@DisplayName("사용자는 이미 등록한 계정 알림 설정은 새로 등록하지 않는다")
	@Test
	void registerDefaultNotificationPreference_whenExistNotificationPreference_thenNotRegister() {
		// given
		Member member = memberRepository.save(createMember());

		// when
		service.registerDefaultNotificationPreference(member);

		// then
		List<NotificationPreference> preferences = repository.findAll();
		assertThat(preferences).hasSize(1);
	}

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
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(
			member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, true, true, true),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, true, true, true)
		);
	}

	@DisplayName("사용자가 계정 설정시 기존 설정이 없다면 새로 등록한다")
	@Test
	void updateNotificationPreference_whenNotExistPreference_thenRegisterPreference() {
		// given
		Member member = createMember();
		member.setNotificationPreference(null);
		member = memberRepository.save(member);

		MemberNotificationPreferenceRequest request = MemberNotificationPreferenceRequest.builder()
			.browserNotify(false)
			.targetGainNotify(false)
			.maxLossNotify(false)
			.targetPriceNotify(false)
			.build();

		setAuthentication(member);
		// when
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false)
		);
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
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
		);
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
		MemberNotificationPreferenceResponse response = service.updateNotificationPreference(member.getId(), request);

		// then
		NotificationPreference preference = repository.findByMemberId(member.getId()).orElseThrow();
		assertAll(
			() -> assertThat(response)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(preference)
				.extracting("browserNotify", "targetGainNotify", "maxLossNotify", "targetPriceNotify")
				.containsExactly(false, false, false, false),
			() -> assertThat(fcmRepository.findAllByMemberId(member.getId())).isEmpty()
		);
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
		Throwable throwable = catchThrowable(() -> service.updateNotificationPreference(member.getId(), request));

		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(member.getNotificationPreference().toString());
	}
}
