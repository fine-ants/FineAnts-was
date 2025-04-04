package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class VerifyCodeInvalidInputException extends InvalidInputException {
	public VerifyCodeInvalidInputException(String value) {
		super(value, ErrorCode.VERIFY_CODE_BAD_REQUEST);
	}
}
