package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class BaseExchangeRateNotFoundException extends NotFoundException {
	public BaseExchangeRateNotFoundException(String value) {
		super(value, CustomErrorCode.BASE_EXCHANGE_RATE_NOT_FOUND);
	}
}
