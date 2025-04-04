package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MemberProfileNotChangeException extends InvalidInputException {
	public MemberProfileNotChangeException(String value) {
		super(value, CustomErrorCode.MEMBER_PROFILE_NOT_CHANGE_BAD_REQUEST);
	}
}
