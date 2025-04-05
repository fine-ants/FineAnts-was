package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PasswordConfirmInvalidInputException extends InvalidInputException {
	public PasswordConfirmInvalidInputException(String passwordConfirm) {
		super(passwordConfirm, ErrorCode.PASSWORD_CONFIRM_BAD_REQUEST);
	}
}
