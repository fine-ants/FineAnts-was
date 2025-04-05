package co.fineants.api.domain.fcm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.fcm.domain.dto.request.FcmRegisterRequest;
import co.fineants.api.domain.fcm.domain.dto.response.FcmDeleteResponse;
import co.fineants.api.domain.fcm.domain.dto.response.FcmRegisterResponse;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.repository.MemberRepository;
import co.fineants.api.global.errors.exception.business.FcmInvalidInputException;
import co.fineants.api.global.errors.exception.business.ForbiddenException;
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

	@DisplayName("사용자는 FCM 토큰을 등록한다")
	@Test
	void registerToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();

		String messageId = "1";
		given(mockedFirebaseMessaging.send(any(Message.class), anyBoolean()))
			.willReturn(messageId);

		// when
		FcmRegisterResponse response = fcmService.createToken(request, member.getId());

		// then
		assertThat(response.getFcmTokenId()).isPositive();
	}

	@DisplayName("한 사용자가 동일한 토큰값으로 여러번의 토큰 등록을 요청해도 db에는 한개의 member_id, token 값쌍의 데이터가 있어야 한다")
	@Test
	void createToken_whenMultipleCreateFcmTokenApi_thenOneFcmToken() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("token")
			.build();

		// 사용자 인증 제공
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		// when
		List<CompletableFuture<FcmRegisterResponse>> futures = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			CompletableFuture<FcmRegisterResponse> future = CompletableFuture.supplyAsync(() -> {
				SecurityContextHolder.getContextHolderStrategy().getContext().setAuthentication(authentication);
				return fcmService.createToken(request, member.getId());
			});
			futures.add(future);
		}

		String messageId = "1";
		given(mockedFirebaseMessaging.send(any(Message.class), anyBoolean()))
			.willReturn(messageId);

		// 10개의 쓰레드가 전부 완료할때까지 대기
		Throwable throwable = catchThrowable(() -> futures.stream()
			.map(CompletableFuture::join)
			.toList());

		// then
		assertThat(throwable)
			.isInstanceOf(CompletionException.class);
		assertThat(fcmRepository.findAllByMemberId(member.getId())).hasSize(1);
	}

	@DisplayName("사용자는 유효하지 않은 FCM 토큰을 등록할 수 없다")
	@Test
	void registerToken_whenInvalidToken_thenThrow400Error() throws FirebaseMessagingException {
		// given
		Member member = memberRepository.save(createMember());
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();

		given(mockedFirebaseMessaging.send(any(Message.class), anyBoolean()))
			.willThrow(FirebaseMessagingException.class);

		// when
		Throwable throwable = catchThrowable(() -> fcmService.createToken(request, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FcmInvalidInputException.class)
			.hasMessage("fcmToken");
	}

	@DisplayName("사용자는 이미 동일한 FCM 토큰이 등록되어 있는 경우 최신 활성화 시간을 업데이트한다")
	@Test
	void registerToken_whenAlreadyFcmToken_thenThrow409Error() {
		// given
		Member member = memberRepository.save(createMember());
		FcmToken token = fcmRepository.save(createFcmToken("fcmToken", member));
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();
		// when
		FcmRegisterResponse response = fcmService.createToken(request, member.getId());

		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting("fcmTokenId")
				.isEqualTo(token.getId()),
			() -> {
				FcmToken findFcmToken = fcmRepository.findById(token.getId()).orElseThrow();
				assertThat(token.getLatestActivationTime().isBefore(findFcmToken.getLatestActivationTime())).isTrue();
			}
		);
	}

	@DisplayName("사용자는 FCM 토큰을 삭제한다")
	@Test
	void deleteToken() {
		// given
		Member member = memberRepository.save(createMember());
		FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));

		setAuthentication(member);
		// when
		FcmDeleteResponse response = fcmService.deleteToken(fcmToken.getId());

		// then
		Assertions.assertAll(
			() -> assertThat(response)
				.extracting("fcmTokenId")
				.isEqualTo(fcmToken.getId()),
			() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
		);
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
		log.info("throwable = {}", throwable);
		log.info("fcmToken = {}", fcmToken);
		assertThat(throwable)
			.isInstanceOf(ForbiddenException.class)
			.hasMessage(fcmToken.toString());
	}
}
