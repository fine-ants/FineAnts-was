package co.fineants.api.global.errors.exception.business;

import org.springframework.http.HttpStatus;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class KisApiRequestException extends BusinessException {
	private final String returnCode;
	private final String messageCode;
	private final String message;

	protected KisApiRequestException(String returnCode, String messageCode, String message, ErrorCode errorCode) {
		super(message, errorCode);
		this.returnCode = returnCode;
		this.messageCode = messageCode;
		this.message = message;
	}

	public static KisApiRequestException requestLimitExceeded() {
		return new RequestLimitExceededKisException("1", "EGW00201", "초당 거래건수를 초과하였습니다.");
	}

	public static KisApiRequestException expiredAccessToken() {
		return new ExpiredAccessTokenKisException("1", "EGW00123", "기간이 만료된 token 입니다.");
	}

	@Override
	public HttpStatus getHttpStatus() {
		return HttpStatus.BAD_REQUEST;
	}

	@Override
	public String getExceptionValue() {
		return String.format("%s %s %s", returnCode, messageCode, message);
	}
}
