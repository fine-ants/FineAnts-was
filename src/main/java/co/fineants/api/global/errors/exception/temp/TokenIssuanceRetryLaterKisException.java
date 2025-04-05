package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class TokenIssuanceRetryLaterKisException extends KisApiRequestException {
	public TokenIssuanceRetryLaterKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message, ErrorCode.KIS_TOKEN_ISSUANCE_RETRY_LATER);
	}
}
