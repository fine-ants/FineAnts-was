package codesquad.fineants.spring.api.fcm.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.FcmSuccessCode;
import codesquad.fineants.spring.api.fcm.request.FcmRegisterRequest;
import codesquad.fineants.spring.api.fcm.response.FcmDeleteResponse;
import codesquad.fineants.spring.api.fcm.response.FcmRegisterResponse;
import codesquad.fineants.spring.api.fcm.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm/tokens")
public class FcmRestController {
	private final FcmService fcmService;

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping
	public ApiResponse<FcmRegisterResponse> createToken(
		@Valid @RequestBody FcmRegisterRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		FcmRegisterResponse response = fcmService.createToken(request, authMember);
		log.info("FCM 토큰 등록 결과 : response={}", response);
		return ApiResponse.success(FcmSuccessCode.CREATED_FCM, response);
	}

	@DeleteMapping("/{fcmTokenId}")
	public ApiResponse<Void> deleteToken(
		@PathVariable Long fcmTokenId,
		@AuthPrincipalMember AuthMember authMember
	) {
		FcmDeleteResponse response = fcmService.deleteToken(fcmTokenId, authMember.getMemberId());
		log.info("FCM 토큰 삭제 결과 : response={}", response);
		return ApiResponse.success(FcmSuccessCode.OK_DELETE_FCM);
	}
}
