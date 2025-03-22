package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PasswordAuthenticationException extends AuthenticationException {

	public PasswordAuthenticationException(String value) {
		super(value, CustomErrorCode.PASSWORD_UNAUTHENTICATED);
	}
}
