package codesquad.fineants.spring.api.errors.errorcode;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

	NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "회원을 찾지 못하였습니다."),
	ALREADY_EXIST_ID(HttpStatus.CONFLICT, "중복된 아이디입니다."),
	REDUNDANT_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임이 중복되었습니다"),
	REDUNDANT_EMAIL(HttpStatus.BAD_REQUEST, "이메일이 중복되었습니다."),
	PASSWORD_CHECK_FAIL(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
	VERIFICATION_CODE_CHECK_FAIL(HttpStatus.BAD_REQUEST, "검증코드가 일치하지 않습니다."),
	SEND_EMAIL_VERIF_FAIL(HttpStatus.BAD_REQUEST, "이메일 전송이 실패하였습니다"),
	PROFILE_IMAGE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "이미지 파일 업로드가 실패하였습니다."),
	BAD_SIGNUP_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력형식입니다."),
	LOGIN_FAIL(HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),
	FORBIDDEN_MEMBER(HttpStatus.FORBIDDEN, "권한이 없습니다.");

	private final HttpStatus httpStatus;
	private final String message;

	@Override
	public String toString() {
		return String.format("%s, %s(name=%s, httpStatus=%s, message=%s)", "회원 에러 코드",
			this.getClass().getSimpleName(),
			name(),
			httpStatus,
			message);
	}
}
