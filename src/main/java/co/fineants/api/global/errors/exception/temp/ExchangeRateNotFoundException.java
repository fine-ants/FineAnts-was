package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ExchangeRateNotFoundException
	extends NotFoundException {
	public ExchangeRateNotFoundException(String value) {
		super(value, CustomErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}
}
