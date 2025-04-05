package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class DefaultKisApiRequestException extends KisApiRequestException {
	public DefaultKisApiRequestException(String returnCode, String messageCode, String message) {
		super(returnCode, messageCode, message, ErrorCode.KIS_DEFAULT_ERROR);
	}
}
