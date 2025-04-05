package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class BaseExchangeRateNotFoundException extends NotFoundException {
	public BaseExchangeRateNotFoundException(String value) {
		super(value, ErrorCode.BASE_EXCHANGE_RATE_NOT_FOUND);
	}
}
