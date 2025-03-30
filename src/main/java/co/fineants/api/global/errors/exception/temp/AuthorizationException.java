package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import lombok.Getter;

@Getter
public class AuthorizationException extends BusinessException {
	private final String value;

	public AuthorizationException(String value, CustomErrorCode errorCode) {
		super(value, errorCode);
		this.value = value;
	}
}
