package co.fineants.api.domain.fcm.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.firebase.messaging.FirebaseMessaging;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class FcmServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private FcmService fcmService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private FcmRepository fcmRepository;

	@Autowired
	private FirebaseMessaging mockedFirebaseMessaging;

	@AfterEach
	void tearDown() {
		Mockito.clearInvocations(mockedFirebaseMessaging);
	}
	
	@DisplayName("사용자는 다른 사용자의 FCM 토큰을 삭제할 수 없다")
	@Test
	void deleteToken_whenOtherMemberRequest_thenThrowException() {
		// given
		Member member = memberRepository.save(createMember());
		Member hacker = memberRepository.save(createMember("hacker"));
		FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));

		setAuthentication(hacker);
		// when
		Throwable throwable = catchThrowable(() -> fcmService.deleteToken(fcmToken.getId()));
		// then
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class);
	}
}
