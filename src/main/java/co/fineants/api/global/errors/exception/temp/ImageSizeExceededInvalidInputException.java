package co.fineants.api.global.errors.exception.temp;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.errorcode.ErrorCode;

public class ImageSizeExceededInvalidInputException extends InvalidInputException {
	public ImageSizeExceededInvalidInputException(MultipartFile file) {
		super(file.toString(), ErrorCode.IMAGE_SIZE_EXCEEDED_BAD_REQUEST);
	}
}
