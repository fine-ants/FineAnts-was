package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageNameEmptyBadRequestException extends BadRequestException {
	public ImageNameEmptyBadRequestException(String value) {
		super(value, CustomErrorCode.IMAGE_NAME_EMPTY_BAD_REQUEST);
	}
}
