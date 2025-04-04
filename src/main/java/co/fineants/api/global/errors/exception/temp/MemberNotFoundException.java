package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class MemberNotFoundException extends NotFoundException {
	public MemberNotFoundException(String value) {
		super(value, ErrorCode.MEMBER_NOT_FOUND);
	}
}
