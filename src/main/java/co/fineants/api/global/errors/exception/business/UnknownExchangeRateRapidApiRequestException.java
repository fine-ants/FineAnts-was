package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class UnknownExchangeRateRapidApiRequestException extends ExchangeRateRapidApiRequestException {
	public UnknownExchangeRateRapidApiRequestException() {
		super("Unknown", "Unknown", ErrorCode.EXCHANGE_RATE_RAPID_API_UNKNOWN);
	}
}
