package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class BaseExchangeRateDeleteInvalidInputException extends InvalidInputException {
	public BaseExchangeRateDeleteInvalidInputException(String value) {
		super(value, CustomErrorCode.BASE_EXCHANGE_RATE_DELETE_BAD_REQUEST);
	}
}
