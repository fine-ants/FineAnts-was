package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MailDuplicateException extends DuplicateException {

	public MailDuplicateException(String value) {
		super(value, CustomErrorCode.EMAIL_DUPLICATE);
	}
}
