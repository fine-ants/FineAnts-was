package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageNameEmptyInvalidInputException extends InvalidInputException {
	public ImageNameEmptyInvalidInputException(String value) {
		super(value, CustomErrorCode.IMAGE_NAME_EMPTY_BAD_REQUEST);
	}
}
