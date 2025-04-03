package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ExchangeRateDuplicateException extends DuplicateException {
	public ExchangeRateDuplicateException(String value){
		super(value, CustomErrorCode.);
	}
}
