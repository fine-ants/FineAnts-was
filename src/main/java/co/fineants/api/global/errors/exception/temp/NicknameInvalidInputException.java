package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class NicknameInvalidInputException extends InvalidInputException {

	public NicknameInvalidInputException(String nickname) {
		super(nickname, CustomErrorCode.NICKNAME_BAD_REQUEST);
	}
}
