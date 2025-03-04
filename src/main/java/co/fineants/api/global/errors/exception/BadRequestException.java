package co.fineants.api.global.errors.exception;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class BadRequestException extends FineAntsException {

	public BadRequestException(ErrorCode errorCode) {
		super(errorCode);
	}

	public BadRequestException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, HttpStatus.BAD_REQUEST, message, cause);
	}
}
