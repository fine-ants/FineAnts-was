package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PasswordConfirmInvalidInputException extends InvalidInputException {
	public PasswordConfirmInvalidInputException(String newPassword, String newPasswordConfirm) {
		super("newPassword=" + newPassword + ", newPasswordConfirm=" + newPasswordConfirm,
			ErrorCode.PASSWORD_CONFIRM_BAD_REQUEST);
	}
}
