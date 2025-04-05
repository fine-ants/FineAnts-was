package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MemberAuthenticationException extends AuthenticationException {
	public MemberAuthenticationException(String value) {
		super(value, ErrorCode.MEMBER_AUTHENTICATION);
	}
}
