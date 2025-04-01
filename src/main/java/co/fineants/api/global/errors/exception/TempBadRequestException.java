package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class TempBadRequestException extends FineAntsException {
	public TempBadRequestException(ErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
