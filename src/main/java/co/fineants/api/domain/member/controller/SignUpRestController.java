package co.fineants.api.domain.member.controller;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.member.domain.dto.request.SignUpRequest;
import co.fineants.api.domain.member.domain.dto.request.SignUpServiceRequest;
import co.fineants.api.domain.member.domain.dto.request.VerifyCodeRequest;
import co.fineants.api.domain.member.domain.dto.request.VerifyEmailRequest;
import co.fineants.api.domain.member.domain.dto.response.SignUpServiceResponse;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.member.domain.entity.MemberProfile;
import co.fineants.api.domain.member.domain.factory.MemberFactory;
import co.fineants.api.domain.member.domain.factory.MemberProfileFactory;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.infra.s3.service.AmazonS3Service;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api")
@RestController
public class SignUpRestController {

	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	private final AmazonS3Service amazonS3Service;
	private final MemberProfileFactory memberProfileFactory;
	private final MemberFactory memberFactory;

	@ResponseStatus(CREATED)
	@PostMapping(value = "/auth/signup", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	@PermitAll
	public ApiResponse<Void> signup(
		@Valid @RequestPart(value = "signupData") SignUpRequest request,
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile
	) {
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, profileImageFile);
		SignUpServiceResponse response = memberService.signup(serviceRequest);
		log.info("일반 회원 가입 컨트롤러 결과 : {}", response);
		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	@ResponseStatus(CREATED)
	@PostMapping(value = "/auth/signup_temp", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	@PermitAll
	public ApiResponse<Void> signup_temp(
		@Valid @RequestPart(value = "signupData") SignUpRequest request,
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile
	) {
		memberService.verifyPasswordMatch(request.getPassword(), request.getPasswordConfirm());
		String encodedPassword = passwordEncoder.encode(request.getPassword());
		String profileUrl = amazonS3Service.upload(profileImageFile);

		MemberProfile profile = memberProfileFactory.localMemberProfile(request.getNickname(), request.getEmail(),
			encodedPassword, profileUrl);
		Member member = memberFactory.localMember(profile);
		memberService.signup(member);
		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	@PostMapping("/auth/signup/verifyEmail")
	@PermitAll
	public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody VerifyEmailRequest request) {
		memberService.sendVerifyCode(request);
		return ApiResponse.success(MemberSuccessCode.OK_SEND_VERIFY_CODE);
	}

	@PostMapping("/auth/signup/verifyCode")
	@PermitAll
	public ApiResponse<Void> checkVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
		memberService.checkVerifyCode(request.email(), request.code());
		return ApiResponse.success(MemberSuccessCode.OK_VERIF_CODE);
	}

	@GetMapping("/auth/signup/duplicationcheck/nickname/{nickname}")
	@PermitAll
	public ApiResponse<Void> nicknameDuplicationCheck(@PathVariable final String nickname) {
		memberService.checkNickname(nickname);
		return ApiResponse.success(MemberSuccessCode.OK_NICKNAME_CHECK);
	}

	@GetMapping("/auth/signup/duplicationcheck/email/{email}")
	@PermitAll
	public ApiResponse<Void> emailDuplicationCheck(@PathVariable final String email) {
		memberService.checkEmail(email);
		return ApiResponse.success(MemberSuccessCode.OK_EMAIL_CHECK);
	}
}
