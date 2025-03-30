package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
	private final CustomErrorCode errorCode;

	protected BusinessException(String message, CustomErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	protected BusinessException(String message, CustomErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public abstract HttpStatus getHttpStatus();

	public abstract String getExceptionValue();

	public String getErrorCodeMessage() {
		return errorCode.getMessage();
	}
}
