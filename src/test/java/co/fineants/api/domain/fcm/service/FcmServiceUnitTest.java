package co.fineants.api.domain.fcm.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.fcm.domain.dto.request.FcmRegisterRequest;
import co.fineants.api.domain.fcm.domain.dto.response.FcmRegisterResponse;
import co.fineants.api.domain.fcm.domain.entity.FcmToken;
import co.fineants.api.domain.fcm.repository.FcmRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.FcmInvalidInputException;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberRepository;

@ExtendWith(MockitoExtension.class)
class FcmServiceUnitTest {
	@Mock
	private MemberRepository memberRepository;
	@Mock
	private FcmRepository fcmRepository;
	@Mock
	private FirebaseMessaging firebaseMessaging;
	@Mock
	private LocalDateTimeService localDateTimeService;
	@InjectMocks
	private FcmService fcmService;

	@DisplayName("FCM 토큰을 저장한다")
	@Test
	void should_save_fcm_token() throws FirebaseMessagingException {
		// given
		Member member = TestDataFactory.createMember();
		BDDMockito.given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		String fcmTokenText = "fcmToken";
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken(fcmTokenText)
			.build();

		String messageId = "1";
		BDDMockito.given(firebaseMessaging.send(any(Message.class), anyBoolean()))
			.willReturn(messageId);

		FcmToken fcmToken = FcmToken.create(member, fcmTokenText);
		BDDMockito.given(fcmRepository.findByTokenAndMemberId(request.getFcmToken(), member.getId()))
			.willReturn(Optional.of(fcmToken));

		LocalDateTime latestActivationTime = LocalDate.of(2026, 7, 17).atStartOfDay();
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(latestActivationTime);

		FcmToken savedFcmToken = FcmToken.create(1L, member, fcmTokenText);
		savedFcmToken.refreshLatestActivationTime(latestActivationTime);
		BDDMockito.given(fcmRepository.save(fcmToken))
			.willReturn(savedFcmToken);
		// when
		FcmRegisterResponse response = fcmService.createToken(request, member.getId());

		// then
		assertThat(response.getFcmTokenId()).isEqualTo(savedFcmToken.getId());
		Assertions.assertThat(savedFcmToken.getLatestActivationTime()).isEqualTo(latestActivationTime);
	}

	@DisplayName("한 사용자가 동일한 토큰값으로 여러번의 토큰 등록을 요청해도 db에는 한개의 member_id, token 값쌍의 데이터가 있어야 한다")
	@Test
	void should_only_one_fcm_token_data_when_multiple_thread_save_fcm_token() throws FirebaseMessagingException {
		// given
		Member member = TestDataFactory.createMember();
		BDDMockito.given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		String messageId = "1";
		BDDMockito.given(firebaseMessaging.send(any(Message.class), anyBoolean()))
			.willReturn(messageId);
		String fcmTokenText = "token";
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken(fcmTokenText)
			.build();
		FcmToken fcmToken = FcmToken.create(member, fcmTokenText);
		BDDMockito.given(fcmRepository.findByTokenAndMemberId(request.getFcmToken(), member.getId()))
			.willReturn(Optional.of(fcmToken));

		LocalDateTime latestActivationTime = LocalDate.of(2026, 7, 17).atStartOfDay();
		BDDMockito.given(localDateTimeService.getLocalDateTimeWithNow())
			.willReturn(latestActivationTime);
		FcmToken savedFcmToken = FcmToken.create(1L, member, fcmTokenText);
		savedFcmToken.refreshLatestActivationTime(latestActivationTime);
		BDDMockito.given(fcmRepository.save(fcmToken))
			.willReturn(savedFcmToken);
		// when
		List<CompletableFuture<FcmRegisterResponse>> futures = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			CompletableFuture<FcmRegisterResponse> future = CompletableFuture.supplyAsync(() -> {
				return fcmService.createToken(request, member.getId());
			});
			futures.add(future);
		}
		// 10개의 쓰레드가 전부 완료할때까지 대기
		List<FcmRegisterResponse> results = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
			.thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
			.join();

		// then
		assertThat(results)
			.hasSize(10)
			.allSatisfy(response -> Assertions.assertThat(response.getFcmTokenId()).isEqualTo(savedFcmToken.getId()));
		Assertions.assertThat(savedFcmToken.getLatestActivationTime()).isEqualTo(latestActivationTime);
	}

	@DisplayName("사용자는 유효하지 않은 FCM 토큰을 등록할 수 없다")
	@Test
	void should_not_save_fcm_token_when_invalid_fcm_token() throws FirebaseMessagingException {
		// given
		Member member = TestDataFactory.createMember();
		BDDMockito.given(memberRepository.findById(member.getId()))
			.willReturn(Optional.of(member));
		FcmRegisterRequest request = FcmRegisterRequest.builder()
			.fcmToken("fcmToken")
			.build();

		BDDMockito.given(firebaseMessaging.send(any(Message.class), anyBoolean()))
			.willThrow(FirebaseMessagingException.class);

		// when
		Throwable throwable = catchThrowable(() -> fcmService.createToken(request, member.getId()));

		// then
		assertThat(throwable)
			.isInstanceOf(FcmInvalidInputException.class)
			.hasMessage("fcmToken");
	}

	// @DisplayName("사용자는 이미 동일한 FCM 토큰이 등록되어 있는 경우 최신 활성화 시간을 업데이트한다")
	// @Test
	// void registerToken_whenAlreadyFcmToken_thenThrow409Error() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	FcmToken token = fcmRepository.save(createFcmToken("fcmToken", member));
	// 	FcmRegisterRequest request = FcmRegisterRequest.builder()
	// 		.fcmToken("fcmToken")
	// 		.build();
	// 	// when
	// 	FcmRegisterResponse response = fcmService.createToken(request, member.getId());
	//
	// 	// then
	// 	Assertions.assertAll(
	// 		() -> assertThat(response)
	// 			.extracting("fcmTokenId")
	// 			.isEqualTo(token.getId()),
	// 		() -> {
	// 			FcmToken findFcmToken = fcmRepository.findById(token.getId()).orElseThrow();
	// 			assertThat(token.getLatestActivationTime().isBefore(findFcmToken.getLatestActivationTime())).isTrue();
	// 		}
	// 	);
	// }
	//
	// @DisplayName("사용자는 FCM 토큰을 삭제한다")
	// @Test
	// void deleteToken() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));
	//
	// 	setAuthentication(member);
	// 	// when
	// 	FcmDeleteResponse response = fcmService.deleteToken(fcmToken.getId());
	//
	// 	// then
	// 	Assertions.assertAll(
	// 		() -> assertThat(response)
	// 			.extracting("fcmTokenId")
	// 			.isEqualTo(fcmToken.getId()),
	// 		() -> assertThat(fcmRepository.findById(fcmToken.getId())).isEmpty()
	// 	);
	// }
	//
	// @DisplayName("사용자는 다른 사용자의 FCM 토큰을 삭제할 수 없다")
	// @Test
	// void deleteToken_whenOtherMemberRequest_thenThrowException() {
	// 	// given
	// 	Member member = memberRepository.save(createMember());
	// 	Member hacker = memberRepository.save(createMember("hacker"));
	// 	FcmToken fcmToken = fcmRepository.save(createFcmToken("fcmToken", member));
	//
	// 	setAuthentication(hacker);
	// 	// when
	// 	Throwable throwable = catchThrowable(() -> fcmService.deleteToken(fcmToken.getId()));
	// 	// then
	// 	assertThat(throwable)
	// 		.isInstanceOf(ForbiddenException.class);
	// }
}
