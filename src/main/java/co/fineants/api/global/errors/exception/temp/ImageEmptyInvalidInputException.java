package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ImageEmptyInvalidInputException extends InvalidInputException {
	public ImageEmptyInvalidInputException() {
		super(null, ErrorCode.IMAGE_EMPTY_BAD_REQUEST);
	}
}
