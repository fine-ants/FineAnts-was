package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class RoleNotFoundException extends NotFoundException {
	public RoleNotFoundException(String value) {
		super(value, ErrorCode.ROLE_NOT_FOUND);
	}
}
