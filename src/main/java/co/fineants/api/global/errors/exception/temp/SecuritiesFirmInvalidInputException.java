package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class SecuritiesFirmInvalidInputException extends InvalidInputException {
	public SecuritiesFirmInvalidInputException(String securitiesFirm) {
		super(securitiesFirm, ErrorCode.SECURITIES_FIRM_BAD_REQUEST);
	}
}
