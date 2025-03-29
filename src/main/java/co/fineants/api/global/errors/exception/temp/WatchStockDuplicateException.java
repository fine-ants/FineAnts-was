package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class WatchStockDuplicateException extends DuplicateException {
	public WatchStockDuplicateException(String value) {
		super(value, CustomErrorCode.WATCH_STOCK_DUPLICATE);
	}
}
