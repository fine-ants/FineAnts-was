package co.fineants.api.global.errors.exception.temp;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.errorcode.CustomErrorCode;

public class ImageSizeExceededBadRequestException extends BadRequestException {
	public ImageSizeExceededBadRequestException(MultipartFile file) {
		super(file.toString(), CustomErrorCode.IMAGE_SIZE_EXCEEDED_BAD_REQUEST);
	}
}
