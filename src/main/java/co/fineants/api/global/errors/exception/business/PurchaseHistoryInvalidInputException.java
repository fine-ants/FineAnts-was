package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class PurchaseHistoryInvalidInputException extends InvalidInputException {
	public PurchaseHistoryInvalidInputException(String value) {
		super(value, ErrorCode.PURCHASE_HISTORY_BAD_REQUEST);
	}
}
