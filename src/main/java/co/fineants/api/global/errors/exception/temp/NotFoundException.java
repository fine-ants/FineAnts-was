package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class NotFoundException extends BusinessException {
	private final String value;

	public NotFoundException(String value, CustomErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}
}
