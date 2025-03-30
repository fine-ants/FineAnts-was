package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import lombok.Getter;

@Getter
public class DuplicateException extends BusinessException {
	private final String value;

	public DuplicateException(String value, CustomErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}

	@Override
	public HttpStatus determineHttpStatus() {
		return HttpStatus.CONFLICT;
	}
}
