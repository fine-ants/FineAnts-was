package co.fineants.api.global.errors.exception.business;

import java.io.File;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ImageWriteInvalidInputException extends InvalidInputException {
	public ImageWriteInvalidInputException(File file) {
		super(file.toString(), ErrorCode.IMAGE_WRITE_BAD_REQUEST);
	}
}
