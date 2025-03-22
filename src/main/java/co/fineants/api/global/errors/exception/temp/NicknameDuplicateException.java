package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class NicknameDuplicateException extends DuplicateException {
	public NicknameDuplicateException(String nickname) {
		super(nickname, CustomErrorCode.NICKNAME_DUPLICATE);
	}
}
