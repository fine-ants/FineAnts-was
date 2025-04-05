package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class HoldingNotFoundException extends NotFoundException {
	public HoldingNotFoundException(String value) {
		super(value, ErrorCode.HOLDING_NOT_FOUND);
	}

	public HoldingNotFoundException(String value, Throwable e) {
		super(value, ErrorCode.HOLDING_NOT_FOUND, e);
	}
}
