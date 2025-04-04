package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class EmailDuplicateException extends DuplicateException {

	public EmailDuplicateException(String email) {
		super(email, ErrorCode.EMAIL_DUPLICATE);
	}
}
