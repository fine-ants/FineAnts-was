package co.fineants.api.global.errors.exception.temp;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final CustomErrorCode errorCode;

	public BusinessException(String message, CustomErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public BusinessException(String message, CustomErrorCode errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public HttpStatus determineHttpStatus() {
		if (this instanceof DuplicateException) {
			return HttpStatus.CONFLICT;
		} else if (this instanceof AuthenticationException) {
			return HttpStatus.UNAUTHORIZED;
		} else if (this instanceof AuthorizationException) {
			return HttpStatus.FORBIDDEN;
		} else if (this instanceof NotFoundException) {
			return HttpStatus.NOT_FOUND;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}
