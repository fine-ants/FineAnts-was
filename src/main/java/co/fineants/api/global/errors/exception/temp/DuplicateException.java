package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateException extends BusinessException {
	private final String value;

	public DuplicateException(String value, ErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	public DuplicateException(String value, ErrorCode errorCode, Throwable cause) {
		super(value, errorCode, cause);
		this.value = value;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.CONFLICT;
	}

	@Override
	public String getExceptionValue() {
		return value;
	}
}
