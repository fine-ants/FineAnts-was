package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class StockNotFoundException extends NotFoundException {
	public StockNotFoundException(String value) {
		super(value, ErrorCode.STOCK_NOT_FOUND);
	}
}
