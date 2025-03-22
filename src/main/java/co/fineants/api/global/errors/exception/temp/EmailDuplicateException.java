package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class EmailDuplicateException extends DuplicateException {

	public EmailDuplicateException(String email) {
		super(email, CustomErrorCode.EMAIL_DUPLICATE);
	}
}
