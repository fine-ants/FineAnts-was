package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class MemberNotFoundException extends NotFoundException {
	public MemberNotFoundException(String value) {
		super(value, CustomErrorCode.MEMBER_NOT_FOUND);
	}
}
