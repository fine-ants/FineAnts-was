package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class InvalidApiKeyExchangeRateRapidApiRequestException extends ExchangeRateRapidApiRequestException {
	public InvalidApiKeyExchangeRateRapidApiRequestException(String returnCode, String message) {
		super(returnCode, message, ErrorCode.EXCHANGE_RATE_RAPID_API_INVALID_API_KEY);
	}
}
