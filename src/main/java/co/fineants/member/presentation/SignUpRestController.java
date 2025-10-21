package co.fineants.member.presentation;

import static org.springframework.http.HttpStatus.*;

import java.util.Set;

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

import co.fineants.api.domain.validator.domain.member.EmailValidator;
import co.fineants.api.domain.validator.domain.member.NicknameValidator;
import co.fineants.api.domain.validator.domain.member.PasswordValidator;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.errors.exception.business.BusinessException;
import co.fineants.api.global.errors.exception.business.SignupException;
import co.fineants.api.global.success.MemberSuccessCode;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.member.application.SendVerificationCode;
import co.fineants.member.application.SignupService;
import co.fineants.member.application.UploadMemberProfileImageFile;
import co.fineants.member.application.VerifyCode;
import co.fineants.member.domain.Member;
import co.fineants.member.domain.MemberEmail;
import co.fineants.member.domain.MemberPassword;
import co.fineants.member.domain.MemberPasswordEncoder;
import co.fineants.member.domain.MemberProfile;
import co.fineants.member.domain.Nickname;
import co.fineants.member.domain.NotificationPreference;
import co.fineants.member.presentation.dto.request.SignUpRequest;
import co.fineants.member.presentation.dto.request.VerifyCodeRequest;
import co.fineants.member.presentation.dto.request.VerifyEmailRequest;
import co.fineants.role.application.FindRole;
import co.fineants.role.domain.Role;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api")
@RestController
public class SignUpRestController {

	private final SignupService signupService;
	private final SendVerificationCode verificationService;
	private final MemberPasswordEncoder memberPasswordEncoder;
	private final UploadMemberProfileImageFile uploadMemberProfileImageFile;
	private final DeleteProfileImageFileService deleteProfileImageFileService;
	private final FindRole findRole;
	private final NicknameValidator nicknameValidator;
	private final EmailValidator emailValidator;
	private final PasswordValidator passwordValidator;
	private final VerifyCode verifyCode;

	@ResponseStatus(CREATED)
	@PostMapping(value = "/auth/signup", consumes = {MediaType.APPLICATION_JSON_VALUE,
		MediaType.MULTIPART_FORM_DATA_VALUE})
	@PermitAll
	public ApiResponse<Void> signup(
		@Valid @RequestPart(value = "signupData") SignUpRequest request,
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile
	) {
		passwordValidator.validateMatch(request.getPassword(), request.getPasswordConfirm());
		String profileUrl = uploadMemberProfileImageFile.upload(profileImageFile).orElse(null);
		MemberEmail memberEmail = new MemberEmail(request.getEmail());
		Nickname nickname = new Nickname(request.getNickname());
		MemberPassword memberPassword = new MemberPassword(request.getPassword(), memberPasswordEncoder);
		MemberProfile profile = MemberProfile.localMemberProfile(memberEmail, nickname, memberPassword,
			profileUrl);
		NotificationPreference notificationPreference = NotificationPreference.defaultSetting();
		Role userRole = findRole.findBy("ROLE_USER");
		Member member = Member.createMember(profile, notificationPreference, Set.of(userRole.getId()));

		try {
			signupService.signup(member);
		} catch (BusinessException exception) {
			log.warn("BusinessException occurred during signup: {}", exception.getMessage(), exception);
			deleteProfileImageFileService.delete(profileUrl);
			throw new SignupException(exception);
		}

		return ApiResponse.success(MemberSuccessCode.OK_SIGNUP);
	}

	@PostMapping("/auth/signup/verifyEmail")
	@PermitAll
	public ApiResponse<Void> sendVerifyCode(@Valid @RequestBody VerifyEmailRequest request) {
		verificationService.send(request.getEmail());
		return ApiResponse.success(MemberSuccessCode.OK_SEND_VERIFY_CODE);
	}

	@PostMapping("/auth/signup/verifyCode")
	@PermitAll
	public ApiResponse<Void> checkVerifyCode(@Valid @RequestBody VerifyCodeRequest request) {
		verifyCode.verifyBy(request.email(), request.code());
		return ApiResponse.success(MemberSuccessCode.OK_VERIF_CODE);
	}

	@GetMapping("/auth/signup/duplicationcheck/nickname/{nickname}")
	@PermitAll
	public ApiResponse<Void> nicknameDuplicationCheck(@PathVariable final String nickname) {
		nicknameValidator.validate(nickname);
		return ApiResponse.success(MemberSuccessCode.OK_NICKNAME_CHECK);
	}

	@GetMapping("/auth/signup/duplicationcheck/email/{email}")
	@PermitAll
	public ApiResponse<Void> emailDuplicationCheck(@PathVariable final String email) {
		emailValidator.validate(email);
		return ApiResponse.success(MemberSuccessCode.OK_EMAIL_CHECK);
	}
}
