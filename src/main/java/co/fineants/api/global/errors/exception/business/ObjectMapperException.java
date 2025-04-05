package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ObjectMapperException extends BusinessException {
	public ObjectMapperException(String message, Throwable cause) {
		super(message, ErrorCode.OBJECT_MAPPER_ERROR, cause);
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@Override
	public String getExceptionValue() {
		return null;
	}
}
