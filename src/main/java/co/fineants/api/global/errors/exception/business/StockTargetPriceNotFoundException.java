package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class StockTargetPriceNotFoundException extends NotFoundException {
	public StockTargetPriceNotFoundException(String value) {
		super(value, ErrorCode.STOCK_TARGET_PRICE_NOT_FOUND);
	}
}
