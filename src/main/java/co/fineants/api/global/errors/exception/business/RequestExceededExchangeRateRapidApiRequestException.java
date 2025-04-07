package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class RequestExceededExchangeRateRapidApiRequestException
	extends ExchangeRateRapidApiRequestException {
	public RequestExceededExchangeRateRapidApiRequestException(String returnCode, String message) {
		super(returnCode, message, ErrorCode.EXCHANGE_RATE_RAPID_API_REQUEST_EXCEEDED);
	}
}
