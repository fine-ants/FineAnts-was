package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class NicknameBadRequestException extends BadRequestException {

	public NicknameBadRequestException(String nickname) {
		super(nickname, CustomErrorCode.NICKNAME_BAD_REQUEST);
	}
}
