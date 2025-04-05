package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class InvalidInputException extends BusinessException {
	private final String value;

	protected InvalidInputException(String value, ErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	protected InvalidInputException(String value, ErrorCode errorCode, Throwable cause) {
		super(value, errorCode, cause);
		this.value = value;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public String getExceptionValue() {
		return this.value;
	}
}
