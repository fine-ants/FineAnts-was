package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ExchangeRateDuplicateException extends DuplicateException {
	public ExchangeRateDuplicateException(String value) {
		super(value, ErrorCode.EXCHANGE_RATE_DUPLICATE);
	}
}
