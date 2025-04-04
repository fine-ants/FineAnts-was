package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class FcmInvalidInputException extends InvalidInputException {
	public FcmInvalidInputException(String value, Throwable cause) {
		super(value, ErrorCode.FCM_BAD_REQUEST, cause);
	}
}
