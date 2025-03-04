package co.fineants.api.global.api;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.FineAntsException;

public class ErrorResponse {
	@JsonProperty
	private final int code;
	@JsonProperty
	private final String status;
	@JsonProperty
	private final String message;
	@JsonProperty
	private final Object data;

	public ErrorResponse(FineAntsException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		HttpStatus httpStatus = errorCode.getHttpStatus();
		this.code = httpStatus.value();
		this.status = httpStatus.getReasonPhrase();
		this.message = errorCode.getMessage();
		this.data = null;
	}

	@Override
	public String toString() {
		return "ErrorResponse(code=%d, status=%s, message=%s)".formatted(code, status, message);
	}
}
