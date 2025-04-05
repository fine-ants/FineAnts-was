package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NicknameDuplicateException extends DuplicateException {
	public NicknameDuplicateException(String nickname) {
		super(nickname, ErrorCode.NICKNAME_DUPLICATE);
	}
}
