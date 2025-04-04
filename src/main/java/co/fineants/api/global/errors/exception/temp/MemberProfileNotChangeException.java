package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MemberProfileNotChangeException extends InvalidInputException {
	public MemberProfileNotChangeException(String value) {
		super(value, ErrorCode.MEMBER_PROFILE_NOT_CHANGE_BAD_REQUEST);
	}
}
