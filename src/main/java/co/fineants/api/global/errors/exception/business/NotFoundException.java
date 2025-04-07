package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundException extends BusinessException {
	private final String value;

	public NotFoundException(String value, ErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	public NotFoundException(String value, ErrorCode errorCode, Throwable cause) {
		super(value, errorCode, cause);
		this.value = value;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.NOT_FOUND;
	}

	@Override
	public String getExceptionValue() {
		return value;
	}
}
