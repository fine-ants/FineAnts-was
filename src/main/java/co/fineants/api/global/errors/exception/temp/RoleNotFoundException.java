package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class RoleNotFoundException extends NotFoundException {
	public RoleNotFoundException(String value) {
		super(value, CustomErrorCode.ROLE_NOT_FOUND);
	}
}
