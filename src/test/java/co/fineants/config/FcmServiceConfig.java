package co.fineants.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.google.firebase.messaging.FirebaseMessaging;

import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.fcm.service.FcmService;
import co.fineants.api.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;

@TestConfiguration
@RequiredArgsConstructor
public class FcmServiceConfig {

	private final FcmRepository fcmRepository;
	private final MemberRepository memberRepository;

	@Bean
	public FcmService fcmService() {
		return new FcmService(fcmRepository, memberRepository, mockedFirebaseMessaging());
	}

	@Bean
	public FirebaseMessaging mockedFirebaseMessaging() {
		return Mockito.mock(FirebaseMessaging.class);
	}
}
