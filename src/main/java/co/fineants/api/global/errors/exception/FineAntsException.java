package co.fineants.api.global.errors.exception;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import lombok.Getter;

@Getter
public class FineAntsException extends RuntimeException {
	private final ErrorCode errorCode;
	// TODO: ErrorCode를 제거하고 HttpStatus만 있어도 될것 같음. message는 생성자로 들어오는거 있으면 받고, 없으면 기본값으로 설정함
	private HttpStatus httpStatus;

	public FineAntsException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public FineAntsException(HttpStatus httpStatus, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = null;
		this.httpStatus = httpStatus;
	}

	public int getHttpStatusCode() {
		return getErrorCode().getHttpStatus().value();
	}

	@Override
	public String toString() {
		return String.format("FineAntsException(errorCode=%s, message=%s)", errorCode, getMessage());
	}
}
