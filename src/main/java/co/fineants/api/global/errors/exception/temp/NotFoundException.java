package co.fineants.api.global.errors.exception.temp;

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
}
