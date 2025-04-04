package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class CashNotSufficientInvalidInputException extends InvalidInputException {
	public CashNotSufficientInvalidInputException(String value) {
		super(value, CustomErrorCode.CASH_NOT_SUFFICIENT_FOR_PURCHASE);
	}
}
