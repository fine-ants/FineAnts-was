package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class ForbiddenException extends BusinessException {
	private final String value;

	public ForbiddenException(String value, ErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.FORBIDDEN;
	}

	@Override
	public String getExceptionValue() {
		return value;
	}
}
