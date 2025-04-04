package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class FcmInvalidInputException extends InvalidInputException {
	public FcmInvalidInputException(String value, Throwable cause) {
		super(value, CustomErrorCode.FCM_BAD_REQUEST, cause);
	}
}
