package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final CustomErrorCode errorCode;

	public BusinessException(String message, CustomErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusinessException(String message, CustomErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
}
