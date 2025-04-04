package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PurchaseHistoryNotFoundException
	extends NotFoundException {
	public PurchaseHistoryNotFoundException(String value) {
		super(value, CustomErrorCode.PURCHASE_HISTORY_NOT_FOUND);
	}
}
