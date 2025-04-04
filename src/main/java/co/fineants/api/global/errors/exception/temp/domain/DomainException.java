package co.fineants.api.global.errors.exception.temp.domain;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class DomainException extends RuntimeException {
	private final CustomErrorCode errorCode;

	protected DomainException(String message, CustomErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	protected DomainException(String message, CustomErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
}
