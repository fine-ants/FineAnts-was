package co.fineants.api.global.errors.exception.temp;

import java.io.File;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageWriteBadRequestException extends BadRequestException {
	public ImageWriteBadRequestException(File file) {
		super(file.toString(), CustomErrorCode.IMAGE_WRITE_BAD_REQUEST);
	}
}
