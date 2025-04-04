package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NicknameDuplicateException extends DuplicateException {
	public NicknameDuplicateException(String nickname) {
		super(nickname, ErrorCode.NICKNAME_DUPLICATE);
	}
}
