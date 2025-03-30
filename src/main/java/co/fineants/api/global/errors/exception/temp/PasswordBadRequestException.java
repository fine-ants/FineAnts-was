package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PasswordBadRequestException extends BadRequestException {
	public PasswordBadRequestException(String value) {
		super(value, CustomErrorCode.PASSWORD_BAD_REQUEST);
	}
}
