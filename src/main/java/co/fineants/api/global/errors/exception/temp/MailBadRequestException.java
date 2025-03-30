package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MailBadRequestException extends BadRequestException {

	public MailBadRequestException(String mail) {
		super(mail, CustomErrorCode.MAIL_BAD_REQUEST);
	}

	public MailBadRequestException(String mail, Throwable throwable) {
		super(mail, CustomErrorCode.MAIL_BAD_REQUEST, throwable);
	}
}
