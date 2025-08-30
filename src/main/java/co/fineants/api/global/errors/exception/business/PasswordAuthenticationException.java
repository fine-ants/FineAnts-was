package co.fineants.api.global.errors.exception.business;

import org.apache.logging.log4j.util.Strings;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PasswordAuthenticationException extends AuthenticationException {

	public PasswordAuthenticationException() {
		super(Strings.EMPTY, ErrorCode.PASSWORD_UNAUTHENTICATED);
	}
}
