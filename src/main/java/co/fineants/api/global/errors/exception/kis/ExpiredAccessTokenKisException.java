package co.fineants.api.global.errors.exception.kis;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.temp.KisApiRequestException;

public class ExpiredAccessTokenKisException extends KisApiRequestException {

	public ExpiredAccessTokenKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message, ErrorCode.KIS_EXPIRED_ACCESS_TOKEN);
	}
}
