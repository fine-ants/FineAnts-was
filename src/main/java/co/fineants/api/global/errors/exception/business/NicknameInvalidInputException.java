package co.fineants.api.global.errors.exception.business;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class NicknameInvalidInputException extends InvalidInputException {

	public NicknameInvalidInputException(String nickname) {
		super(nickname, ErrorCode.NICKNAME_BAD_REQUEST);
	}
}
