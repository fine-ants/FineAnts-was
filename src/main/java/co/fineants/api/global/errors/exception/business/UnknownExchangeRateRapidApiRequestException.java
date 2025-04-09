package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class UnknownExchangeRateRapidApiRequestException extends ExchangeRateRapidApiRequestException {
	public UnknownExchangeRateRapidApiRequestException() {
		super("Unknown", "Unknown", ErrorCode.EXCHANGE_RATE_RAPID_API_UNKNOWN);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}
