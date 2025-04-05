package co.fineants.api.global.errors.exception.kis;

import co.fineants.api.global.errors.errorcode.ErrorCode;
import co.fineants.api.global.errors.exception.temp.KisApiRequestException;

public class CredentialsTypeKisException extends KisApiRequestException {

	public CredentialsTypeKisException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message, ErrorCode.KIS_CREDENTIALS_TYPE_ERROR);
	}
}
