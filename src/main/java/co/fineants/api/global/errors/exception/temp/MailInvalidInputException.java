package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MailInvalidInputException extends InvalidInputException {

	public MailInvalidInputException(String mail) {
		super(mail, CustomErrorCode.MAIL_BAD_REQUEST);
	}

	public MailInvalidInputException(String mail, Throwable throwable) {
		super(mail, CustomErrorCode.MAIL_BAD_REQUEST, throwable);
	}
}
