package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class EmailBadRequestException extends BadRequestException {
	public EmailBadRequestException(String email) {
		super(email, CustomErrorCode.EMAIL_BAD_REQUEST);
	}
}
