package co.fineants.api.global.api;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;

public class ErrorResponse {
	private final int code;
	private final String status;
	private final String message;

	public ErrorResponse(FineAntsException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		HttpStatus httpStatus = errorCode.getHttpStatus();
		this.code = httpStatus.value();
		this.status = httpStatus.getReasonPhrase();
		this.message = errorCode.getMessage();
	}

	@Override
	public String toString() {
		return "ErrorResponse(code=%d, status=%s, message=%s)".formatted(code, status, message);
	}
}
