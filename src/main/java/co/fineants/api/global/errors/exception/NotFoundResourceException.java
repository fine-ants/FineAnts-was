package co.fineants.api.global.errors.exception;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NotFoundResourceException extends FineAntsException {
	private final String message;

	public NotFoundResourceException(ErrorCode errorCode) {
		this(errorCode, errorCode.getMessage());
	}

	public NotFoundResourceException(ErrorCode errorCode, String message) {
		super(errorCode);
		this.message = message;
	}

	@Override
	public String toString() {
		return "NotFoundResourceException(%s, message=%s)".formatted(super.toString(), message);
	}
}
