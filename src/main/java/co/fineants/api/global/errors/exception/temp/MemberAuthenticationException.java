package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MemberAuthenticationException extends AuthenticationException {
	public MemberAuthenticationException(String value) {
		super(value, CustomErrorCode.MEMBER_AUTHENTICATION);
	}
}
