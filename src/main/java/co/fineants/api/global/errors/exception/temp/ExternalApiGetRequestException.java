package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ExternalApiGetRequestException extends ExternalApiRequestException {
	public ExternalApiGetRequestException(String value, HttpStatus httpStatus) {
		super(value, CustomErrorCode.EXTERNAL_API_GET_REQUEST, httpStatus);
	}
}
