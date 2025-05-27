package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class SignupException extends BusinessException {
	private final String value;

	public SignupException(String value, Exception cause) {
		super(value, ErrorCode.SIGNUP_FAIL, cause);
		this.value = value;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public String getExceptionValue() {
		return value;
	}
}
