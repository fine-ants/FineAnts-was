package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class VerifyCodeBadRequestException extends BadRequestException {
	public VerifyCodeBadRequestException(String value) {
		super(value, CustomErrorCode.VERIFY_CODE_BAD_REQUEST);
	}
}
