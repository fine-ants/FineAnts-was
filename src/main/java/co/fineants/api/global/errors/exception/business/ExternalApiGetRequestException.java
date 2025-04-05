package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ExternalApiGetRequestException extends ExternalApiRequestException {
	public ExternalApiGetRequestException(String value, HttpStatus httpStatus) {
		super(value, ErrorCode.EXTERNAL_API_GET_REQUEST, httpStatus);
	}
}
