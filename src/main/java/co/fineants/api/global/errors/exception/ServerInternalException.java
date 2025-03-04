package co.fineants.api.global.errors.exception;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ServerInternalException extends FineAntsException {

	public ServerInternalException(ErrorCode errorCode, String message, Throwable throwable) {
		super(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, message, throwable);
	}
}
