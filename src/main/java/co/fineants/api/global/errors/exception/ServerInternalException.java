package co.fineants.api.global.errors.exception;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ServerInternalException extends FineAntsException {

	public ServerInternalException(ErrorCode errorCode, HttpStatus httpStatus, String message, Throwable throwable) {
		super(errorCode, httpStatus, message, throwable);
	}
}
