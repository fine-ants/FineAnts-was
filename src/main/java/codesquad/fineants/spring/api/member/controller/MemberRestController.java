package codesquad.fineants.spring.api.member.controller;

import static org.springframework.http.HttpStatus.*;

import java.time.LocalDateTime;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import codesquad.fineants.domain.oauth.support.AuthMember;
import codesquad.fineants.domain.oauth.support.AuthPrincipalMember;
import codesquad.fineants.spring.api.common.response.ApiResponse;
import codesquad.fineants.spring.api.common.success.MemberSuccessCode;
import codesquad.fineants.spring.api.common.success.OauthSuccessCode;
import codesquad.fineants.spring.api.member.request.LoginRequest;
import codesquad.fineants.spring.api.member.request.ModifyPasswordRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLoginRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberLogoutRequest;
import codesquad.fineants.spring.api.member.request.OauthMemberRefreshRequest;
import codesquad.fineants.spring.api.member.request.ProfileChangeRequest;
import codesquad.fineants.spring.api.member.request.SignUpRequest;
import codesquad.fineants.spring.api.member.request.VerifyCodeRequest;
import codesquad.fineants.spring.api.member.request.VerifyEmailRequest;
import codesquad.fineants.spring.api.member.response.LoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberLoginResponse;
import codesquad.fineants.spring.api.member.response.OauthMemberRefreshResponse;
import codesquad.fineants.spring.api.member.response.OauthSaveUrlResponse;
import codesquad.fineants.spring.api.member.response.ProfileChangeResponse;
import codesquad.fineants.spring.api.member.response.ProfileResponse;
import codesquad.fineants.spring.api.member.service.MemberService;
import codesquad.fineants.spring.api.member.service.request.ProfileChangeServiceRequest;
import codesquad.fineants.spring.api.member.service.request.SignUpServiceRequest;
import codesquad.fineants.spring.api.member.service.response.SignUpServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api")
@RestController
public class MemberRestController {

	private final MemberService memberService;

	@PostMapping("/auth/{provider}/authUrl")
	public ApiResponse<OauthSaveUrlResponse> saveAuthorizationCodeURL(@PathVariable final String provider) {
		return ApiResponse.success(OauthSuccessCode.OK_URL, memberService.saveAuthorizationCodeURL(provider));
	}

	@PostMapping(value = "/auth/{provider}/login")
	public ApiResponse<OauthMemberLoginResponse> login(@PathVariable final String provider,
		@RequestParam final String code, @RequestParam final String redirectUrl, @RequestParam final String state) {
		log.info("로그인 컨트롤러 요청 : provider = {}, code = {}, redirectUrl = {}, state = {}", provider, code, redirectUrl,
			state);
		return ApiResponse.success(OauthSuccessCode.OK_LOGIN,
			memberService.login(OauthMemberLoginRequest.of(provider, code, redirectUrl, state, LocalDateTime.now())));
	}

	@PostMapping(value = "/auth/logout")
	public ApiResponse<Void> logout(@RequestAttribute final String accessToken,
		@RequestBody final OauthMemberLogoutRequest request) {
		memberService.logout(accessToken, request);
		return ApiResponse.success(OauthSuccessCode.OK_LOGOUT);
	}

	@ResponseStatus(OK)
	@PostMapping("/auth/refresh/token")
	public ApiResponse<OauthMemberRefreshResponse> refreshAccessToken(
		@RequestBody final OauthMemberRefreshRequest request) {
		OauthMemberRefreshResponse response = memberService.refreshAccessToken(request, LocalDateTime.now());
		return ApiResponse.success(OauthSuccessCode.OK_REFRESH_TOKEN, response);
	}

	@ResponseStatus(CREATED)
	@PostMapping(value = "/auth/signup", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	public ApiResponse<Void> signup(
		@Valid @RequestPart(value = "signupData") SignUpRequest request,
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile) {
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, profileImageFile);
		SignUpServiceResponse response = memberService.signup(serviceRequest);
		log.info("일반 회원 가입 컨트롤러 결과 : {}", response);
		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	@PostMapping("/auth/signup/verifyEmail")
	public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody VerifyEmailRequest request) {
		memberService.sendVerifyCode(request);
		return ApiResponse.success(MemberSuccessCode.OK_SEND_VERIFY_CODE);
	}

	@PostMapping("/auth/signup/verifyCode")
	public ApiResponse<Void> checkVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
		memberService.checkVerifyCode(request);
		return ApiResponse.success(MemberSuccessCode.OK_VERIF_CODE);
	}

	@GetMapping("/auth/signup/duplicationcheck/nickname/{nickname}")
	public ApiResponse<Void> nicknameDuplicationCheck(@PathVariable final String nickname) {
		memberService.checkNickname(nickname);
		return ApiResponse.success(MemberSuccessCode.OK_NICKNAME_CHECK);
	}

	@GetMapping("/auth/signup/duplicationcheck/email/{email}")
	public ApiResponse<Void> emailDuplicationCheck(@PathVariable final String email) {
		memberService.checkEmail(email);
		return ApiResponse.success(MemberSuccessCode.OK_EMAIL_CHECK);
	}

	@PostMapping("/auth/login")
	public ApiResponse<LoginResponse> login(@RequestBody final LoginRequest request) {
		return ApiResponse.success(MemberSuccessCode.OK_LOGIN, memberService.login(request));
	}

	@PostMapping(value = "/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public ApiResponse<ProfileChangeResponse> changeProfile(
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile,
		@Valid @RequestPart(value = "profileInformation", required = false) ProfileChangeRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(
			profileImageFile,
			request,
			authMember.getMemberId()
		);
		return ApiResponse.success(MemberSuccessCode.OK_MODIFIED_PROFILE,
			memberService.changeProfile(serviceRequest));
	}

	@GetMapping(value = "/profile")
	public ApiResponse<ProfileResponse> readProfile(
		@AuthPrincipalMember AuthMember authMember) {
		return ApiResponse.success(MemberSuccessCode.OK_READ_PROFILE,
			memberService.readProfile(authMember));
	}

	@PutMapping("/account/password")
	public ApiResponse<Void> changePassword(@RequestBody ModifyPasswordRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		memberService.modifyPassword(request, authMember);
		return ApiResponse.success(MemberSuccessCode.OK_PASSWORD_CHANGED);
	}

	@DeleteMapping("/account")
	public ApiResponse<Void> deleteAccount(@RequestBody final OauthMemberLogoutRequest request,
		@AuthPrincipalMember AuthMember authMember) {
		memberService.logout(authMember.getAccessToken(), request);
		memberService.deleteMember(authMember);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ACCOUNT);
	}
}
