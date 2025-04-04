package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ImageNameEmptyInvalidInputException extends InvalidInputException {
	public ImageNameEmptyInvalidInputException(String value) {
		super(value, ErrorCode.IMAGE_NAME_EMPTY_BAD_REQUEST);
	}
}
