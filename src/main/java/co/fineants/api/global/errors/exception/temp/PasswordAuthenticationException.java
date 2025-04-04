package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PasswordAuthenticationException extends AuthenticationException {

	public PasswordAuthenticationException(String value) {
		super(value, ErrorCode.PASSWORD_UNAUTHENTICATED);
	}
}
