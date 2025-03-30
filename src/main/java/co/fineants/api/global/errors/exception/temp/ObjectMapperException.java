package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ObjectMapperException extends BusinessException {
	public ObjectMapperException(String message, Throwable cause) {
		super(message, CustomErrorCode.OBJECT_MAPPER_ERROR, cause);
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
