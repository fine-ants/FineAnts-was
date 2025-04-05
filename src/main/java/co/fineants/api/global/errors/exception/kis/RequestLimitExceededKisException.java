package co.fineants.api.global.errors.exception.kis;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.temp.KisApiRequestException;

public class RequestLimitExceededKisException extends KisApiRequestException {
	public RequestLimitExceededKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message, ErrorCode.KIS_REQUEST_LIMIT_EXCEEDED);
	}
}
