package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ObjectMapperException extends BusinessException {
	public ObjectMapperException(String message, Throwable cause) {
		super(message, CustomErrorCode.OBJECT_MAPPER_ERROR, cause);
	}
}
