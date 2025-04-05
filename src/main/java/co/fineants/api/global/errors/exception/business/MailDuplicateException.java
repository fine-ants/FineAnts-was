package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MailDuplicateException extends DuplicateException {

	public MailDuplicateException(String value) {
		super(value, ErrorCode.EMAIL_DUPLICATE);
	}
}
