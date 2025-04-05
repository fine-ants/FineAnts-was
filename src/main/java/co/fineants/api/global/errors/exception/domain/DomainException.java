package co.fineants.api.global.errors.exception.domain;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class DomainException extends RuntimeException {
	private final ErrorCode errorCode;

	protected DomainException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	protected DomainException(String message, ErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
}
