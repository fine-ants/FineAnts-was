package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class CashNotSufficientInvalidInputException extends InvalidInputException {
	public CashNotSufficientInvalidInputException(String value) {
		super(value, ErrorCode.CASH_NOT_SUFFICIENT_FOR_PURCHASE);
	}
}
