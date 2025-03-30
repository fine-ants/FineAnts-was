package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageEmptyInvalidInputException extends InvalidInputException {
	public ImageEmptyInvalidInputException() {
		super(null, CustomErrorCode.IMAGE_EMPTY_BAD_REQUEST);
	}
}
