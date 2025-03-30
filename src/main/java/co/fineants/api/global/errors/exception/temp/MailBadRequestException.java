package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MailBadRequestException extends BadRequestException {

	public MailBadRequestException(String value) {
		super(value, CustomErrorCode.MAIL_BAD_REQUEST);
	}
}
