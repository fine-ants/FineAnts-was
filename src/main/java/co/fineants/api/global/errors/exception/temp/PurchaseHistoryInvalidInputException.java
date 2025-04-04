package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class PurchaseHistoryInvalidInputException extends InvalidInputException {
	public PurchaseHistoryInvalidInputException(String value) {
		super(value, CustomErrorCode.PURCHASE_HISTORY_BAD_REQUEST);
	}
}
