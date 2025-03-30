package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PasswordInvalidInputException extends InvalidInputException {
	public PasswordInvalidInputException(String value) {
		super(value, CustomErrorCode.PASSWORD_BAD_REQUEST);
	}
}
