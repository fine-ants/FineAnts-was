package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class StockTargetPriceNotFoundException extends NotFoundException {
	public StockTargetPriceNotFoundException(String value) {
		super(value, CustomErrorCode.STOCK_TARGET_PRICE_NOT_FOUND);
	}
}
