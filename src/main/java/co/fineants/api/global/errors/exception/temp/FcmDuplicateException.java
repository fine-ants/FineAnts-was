package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class FcmDuplicateException extends DuplicateException {
	public FcmDuplicateException(String value, Throwable e) {
		super(value, CustomErrorCode.DUPLICATE_FCM_TOKEN, e);
	}
}
