package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import lombok.Getter;

@Getter
public class NotFoundException extends BusinessException {
	private final String value;

	public NotFoundException(String value, CustomErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	public NotFoundException(String value, CustomErrorCode errorCode, Throwable cause) {
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
