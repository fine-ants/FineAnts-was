package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class WatchStockDuplicateException extends DuplicateException {
	public WatchStockDuplicateException(String value) {
		super(value, ErrorCode.WATCH_STOCK_DUPLICATE);
	}
}
