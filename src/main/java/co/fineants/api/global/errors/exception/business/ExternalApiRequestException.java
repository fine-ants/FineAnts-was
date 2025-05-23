package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ExternalApiRequestException extends BusinessException {
	private final String value;
	private final HttpStatus httpStatus;

	protected ExternalApiRequestException(String value, ErrorCode errorCode, HttpStatus httpStatus) {
		super(value, errorCode);
		this.value = value;
		this.httpStatus = httpStatus;
	}

	protected ExternalApiRequestException(String value, ErrorCode errorCode, HttpStatus httpStatus, Throwable cause) {
		super(value, errorCode, cause);
		this.value = value;
		this.httpStatus = httpStatus;
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
