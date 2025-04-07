package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ExchangeRateRapidApiRequestException extends BusinessException {
	private final String returnCode;
	private final String message;

	protected ExchangeRateRapidApiRequestException(String returnCode, String message, ErrorCode errorCode) {
		super(message, errorCode);
		this.returnCode = returnCode;
		this.message = message;
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public String getExceptionValue() {
		return String.format("%s %s", returnCode, message);
	}
}
