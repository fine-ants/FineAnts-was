package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class BaseExchangeRateDeleteInvalidInputException extends InvalidInputException {
	public BaseExchangeRateDeleteInvalidInputException(String value) {
		super(value, ErrorCode.BASE_EXCHANGE_RATE_DELETE_BAD_REQUEST);
	}
}
