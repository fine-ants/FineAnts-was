package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class RequestExceededExchangeRateRapidApiRequestException
	extends ExchangeRateRapidApiRequestException {
	public RequestExceededExchangeRateRapidApiRequestException(String returnCode, String message) {
		super(returnCode, message, ErrorCode.EXCHANGE_RATE_RAPID_API_REQUEST_EXCEEDED);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.SERVICE_UNAVAILABLE;
	}
}
