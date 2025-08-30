package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

public class SignupException extends BusinessException {
	private final String value;
	private final HttpStatus httpStatus;

	public SignupException(BusinessException cause) {
		super(cause.getMessage(), cause.getErrorCode(), cause);
		this.value = cause.getMessage();
		this.httpStatus = cause.getHttpStatus();
	}

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	@Override
	public String getExceptionValue() {
		return value;
	}
}
