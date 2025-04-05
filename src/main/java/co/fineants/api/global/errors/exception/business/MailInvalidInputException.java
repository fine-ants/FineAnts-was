package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MailInvalidInputException extends InvalidInputException {

	public MailInvalidInputException(String mail) {
		super(mail, ErrorCode.MAIL_BAD_REQUEST);
	}

	public MailInvalidInputException(String mail, Throwable throwable) {
		super(mail, ErrorCode.MAIL_BAD_REQUEST, throwable);
	}
}
