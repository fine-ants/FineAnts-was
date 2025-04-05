package co.fineants.api.global.errors.exception.kis;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.temp.KisApiRequestException;

public class TokenIssuanceRetryLaterKisException extends KisApiRequestException {
	public TokenIssuanceRetryLaterKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message, ErrorCode.KIS_TOKEN_ISSUANCE_RETRY_LATER);
	}
}
