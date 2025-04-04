package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PasswordInvalidInputException extends InvalidInputException {
	public PasswordInvalidInputException(String value) {
		super(value, ErrorCode.PASSWORD_BAD_REQUEST);
	}
}
