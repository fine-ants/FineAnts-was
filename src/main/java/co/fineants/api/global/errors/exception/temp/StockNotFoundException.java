package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class StockNotFoundException extends NotFoundException {
	public StockNotFoundException(String value) {
		super(value, CustomErrorCode.STOCK_NOT_FOUND);
	}
}
