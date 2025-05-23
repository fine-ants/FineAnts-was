package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class EmailInvalidInputException extends InvalidInputException {
	public EmailInvalidInputException(String email) {
		super(email, ErrorCode.EMAIL_BAD_REQUEST);
	}
}
