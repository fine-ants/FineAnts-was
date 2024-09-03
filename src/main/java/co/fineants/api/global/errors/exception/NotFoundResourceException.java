package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NotFoundResourceException extends FineAntsException {

	public NotFoundResourceException(ErrorCode errorCode) {
		super(errorCode);
	}

	public NotFoundResourceException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}