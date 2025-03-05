package co.fineants.api.domain.member.controller;

import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.member.domain.dto.request.PasswordModifyRequest;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeRequest;
import co.fineants.api.domain.member.domain.dto.request.ProfileChangeServiceRequest;
import co.fineants.api.domain.member.domain.dto.response.ProfileChangeResponse;
import co.fineants.api.domain.member.domain.dto.response.ProfileResponse;
import co.fineants.api.domain.member.service.MemberService;
import co.fineants.api.global.api.ApiResponse;
import co.fineants.api.global.errors.exception.BadRequestException;
import co.fineants.api.global.errors.exception.member.NotFoundMemberException;
import co.fineants.api.global.errors.exception.member.PasswordMismatchException;
import co.fineants.api.global.security.oauth.dto.MemberAuthentication;
import co.fineants.api.global.security.oauth.resolver.MemberAuthenticationPrincipal;
import co.fineants.api.global.success.MemberSuccessCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api")
@RestController
public class MemberRestController {

	private final MemberService memberService;

	@PostMapping(value = "/profile", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
	public ApiResponse<ProfileChangeResponse> changeProfile(
		@RequestPart(value = "profileImageFile", required = false) MultipartFile profileImageFile,
		@Valid @RequestPart(value = "profileInformation", required = false) ProfileChangeRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) {
		String nickname = Strings.EMPTY;
		if (request != null) {
			nickname = request.nickname();
		}
		ProfileChangeServiceRequest serviceRequest = ProfileChangeServiceRequest.of(profileImageFile, nickname,
			authentication.getId()
		);
		return ApiResponse.success(MemberSuccessCode.OK_MODIFIED_PROFILE,
			memberService.changeProfile(serviceRequest));
	}

	@GetMapping(value = "/profile")
	public ApiResponse<ProfileResponse> readProfile(
		@MemberAuthenticationPrincipal MemberAuthentication authentication) {
		Long memberId = authentication.getId();
		return ApiResponse.success(MemberSuccessCode.OK_READ_PROFILE,
			memberService.readProfile(memberId));
	}

	/**
	 * 비밀번호를 변경한다.
	 *
	 * @param request 비밀번호 변경 요청
	 * @param authentication 사용자 인증 정보
	 * @return 비밀번호 변경 완료 응답
	 * @throws BadRequestException 비밀번호를 변경하지 못하면 예외가 발생함
	 */
	@PutMapping("/account/password")
	public ApiResponse<Void> changePassword(
		@RequestBody PasswordModifyRequest request,
		@MemberAuthenticationPrincipal MemberAuthentication authentication
	) throws BadRequestException {
		try {
			memberService.modifyPassword(request, authentication.getId());
		} catch (PasswordMismatchException | NotFoundMemberException e) {
			String message = "can't change the password";
			throw new BadRequestException(message, e);
		}
		return ApiResponse.success(MemberSuccessCode.OK_PASSWORD_CHANGED);
	}

	/**
	 * 사용자 계정을 삭제한다
	 *
	 * @param authentication 사용자 인증 정보
	 * @param servletRequest HTTP Request 정보
	 * @param servletResponse HTTP Response 정보
	 * @return 계정 삭제 완료 메시지 응답
	 * @throws BadRequestException 계정 삭제를 하지 못하면 예외 발생함
	 */
	@DeleteMapping("/account")
	public ApiResponse<Void> deleteAccount(
		@MemberAuthenticationPrincipal MemberAuthentication authentication,
		HttpServletRequest servletRequest,
		HttpServletResponse servletResponse
	) throws BadRequestException {
		try {
			memberService.deleteMember(authentication.getId());
		} catch (NotFoundMemberException e) {
			String message = "can't delete the member";
			throw new BadRequestException(message, e);
		}

		memberService.logout(servletRequest, servletResponse);
		return ApiResponse.success(MemberSuccessCode.OK_DELETED_ACCOUNT);
	}
}
