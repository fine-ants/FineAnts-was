package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class SecuritiesFirmInvalidInputException extends InvalidInputException {
	public SecuritiesFirmInvalidInputException(String securitiesFirm) {
		super(securitiesFirm, CustomErrorCode.SECURITIES_FIRM_BAD_REQUEST);
	}
}
