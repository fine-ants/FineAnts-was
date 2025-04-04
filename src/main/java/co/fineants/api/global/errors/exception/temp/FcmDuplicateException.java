package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class FcmDuplicateException extends DuplicateException {
	public FcmDuplicateException(String value, Throwable e) {
		super(value, ErrorCode.FCM_DUPLICATE, e);
	}
}
