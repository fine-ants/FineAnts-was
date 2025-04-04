package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PurchaseHistoryNotFoundException
	extends NotFoundException {
	public PurchaseHistoryNotFoundException(String value) {
		super(value, ErrorCode.PURCHASE_HISTORY_NOT_FOUND);
	}
}
