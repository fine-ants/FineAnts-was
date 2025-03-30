package co.fineants.api.global.errors.exception.temp;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageEmptyBadRequestException extends BadRequestException {
	public ImageEmptyBadRequestException() {
		super(null, CustomErrorCode.IMAGE_EMPTY_BAD_REQUEST);
	}
}
