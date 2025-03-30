package co.fineants.api.global.errors.exception.temp;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageSizeExceededInvalidInputException extends InvalidInputException {
	public ImageSizeExceededInvalidInputException(MultipartFile file) {
		super(file.toString(), CustomErrorCode.IMAGE_SIZE_EXCEEDED_BAD_REQUEST);
	}
}
