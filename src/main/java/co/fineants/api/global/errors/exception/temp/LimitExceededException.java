package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class LimitExceededException extends BusinessException {
	private final String value;

	protected LimitExceededException(String value, CustomErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public String getExceptionValue() {
		return this.value;
	}
}
