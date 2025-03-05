package co.fineants.api.domain.member.controller;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.MediaType;
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
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.ServerInternalException;
import co.fineants.api.global.errors.exception.member.DuplicateEmailException;
import co.fineants.api.global.errors.exception.member.DuplicateNicknameException;
import co.fineants.api.global.errors.exception.member.EmailVerificationSendException;
import co.fineants.api.global.errors.exception.member.InvalidMemberEmailException;
import co.fineants.api.global.errors.exception.member.InvalidMemberNicknameException;
import co.fineants.api.global.errors.exception.member.PasswordMismatchException;
import co.fineants.api.global.success.MemberSuccessCode;
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

	/**
	 * 사용자가 회원가입을 한다
	 *
	 * @param request 회원가입 정보
	 * @param profileImageFile 프로필 이미지 파일(선택)
	 * @return 회원가입 결과
	 * @throws DuplicateEmailException 이메일이 중복되면 예외가 발생함
	 */
	@ResponseStatus(CREATED)
	@PostMapping(value = "/auth/signup", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	@PermitAll
	public ApiResponse<Void> signup(
		@Valid @RequestPart(value = "signupData") SignUpRequest request,
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile
	) throws DuplicateEmailException {
		SignUpServiceRequest serviceRequest = SignUpServiceRequest.of(request, profileImageFile);
		try {
			SignUpServiceResponse response = memberService.signup(serviceRequest);
			log.info("local signup result : {}", response);
		} catch (DuplicateEmailException | PasswordMismatchException exception) {
			String message = "can't signup";
			throw new BadRequestException(message, exception);
		}
		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	/**
	 * 회원가입을 위한 검증 코드를 이메일로 전송한다
	 *
	 * @param request 수신자 이메일가 담긴 요청 정보
	 * @return 검증 코드 전송 완료 결과
	 * @throws ServerInternalException 이메일 전송이 실패하면 예외가 발생함
	 */
	@PostMapping("/auth/signup/verifyEmail")
	@PermitAll
	public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody VerifyEmailRequest request) throws
		ServerInternalException {
		try {
			memberService.sendVerifyCode(request);
		} catch (EmailVerificationSendException e) {
			String message = "can't send verification code for signUp";
			throw new ServerInternalException(null, message, e);
		}
		return ApiResponse.success(MemberSuccessCode.OK_SEND_VERIFY_CODE);
	}

	@PostMapping("/auth/signup/verifyCode")
	@PermitAll
	public ApiResponse<Void> checkVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
		memberService.checkVerifyCode(request.email(), request.code());
		return ApiResponse.success(MemberSuccessCode.OK_VERIF_CODE);
	}

	/**
	 * 회원가입 서비스 중 닉네임이 사용 가능한지 확인한다.
	 *
	 * @param nickname 닉네임
	 * @return 닉네임 사용 가능 응답
	 * @throws BadRequestException 닉네임 사용이 불가능하면 예외가 발생함
	 */
	@GetMapping("/auth/signup/duplicationcheck/nickname/{nickname}")
	@PermitAll
	public ApiResponse<Void> nicknameDuplicationCheck(@PathVariable final String nickname) throws BadRequestException {
		try {
			memberService.checkNickname(nickname);
		} catch (InvalidMemberNicknameException e) {
			String message = "invalid nickname";
			throw new BadRequestException(message, e);
		} catch (DuplicateNicknameException e) {
			String message = "duplicated nickname";
			throw new BadRequestException(message, e);
		}

		return ApiResponse.success(MemberSuccessCode.OK_NICKNAME_CHECK);
	}

	/**
	 * 이메일이 중복되었는지 확인한다
	 *
	 * @param email 이메일
	 * @return 이메일 사용 가능 응답
	 * @throws BadRequestException 이메일 사용이 불가능하면 예외가 발생함
	 */
	@GetMapping("/auth/signup/duplicationcheck/email/{email}")
	@PermitAll
	public ApiResponse<Void> emailDuplicationCheck(@PathVariable final String email) throws BadRequestException {
		try {
			memberService.checkEmail(email);
		} catch (InvalidMemberEmailException e) {
			String message = "email is invalid pattern, email=%s".formatted(email);
			throw new BadRequestException(message, e);
		} catch (DuplicateEmailException e) {
			String message = "email is duplicated, email=%s".formatted(email);
			throw new BadRequestException(message, e);
		}

		return ApiResponse.success(MemberSuccessCode.OK_EMAIL_CHECK);
	}
}
