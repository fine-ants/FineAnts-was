package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PasswordConfirmInvalidInputException extends InvalidInputException {
	public PasswordConfirmInvalidInputException(String passwordConfirm) {
		super(passwordConfirm, CustomErrorCode.PASSWORD_CONFIRM_BAD_REQUEST);
	}
}
