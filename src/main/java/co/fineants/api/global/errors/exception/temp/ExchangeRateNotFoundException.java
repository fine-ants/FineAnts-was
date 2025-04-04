package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ExchangeRateNotFoundException
	extends NotFoundException {
	public ExchangeRateNotFoundException(String value) {
		super(value, ErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}
}
