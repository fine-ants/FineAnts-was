package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class HoldingNotFoundException extends NotFoundException {
	public HoldingNotFoundException(String value) {
		super(value, CustomErrorCode.HOLDING_NOT_FOUND);
	}

	public HoldingNotFoundException(String value, Throwable e) {
		super(value, CustomErrorCode.HOLDING_NOT_FOUND, e);
	}
}
