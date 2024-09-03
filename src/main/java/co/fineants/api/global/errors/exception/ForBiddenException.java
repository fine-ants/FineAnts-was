package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ForBiddenException extends FineAntsException {

	public ForBiddenException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ForBiddenException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}