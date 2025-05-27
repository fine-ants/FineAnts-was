package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.business.ImageEmptyInvalidInputException;
import co.fineants.api.global.errors.exception.business.InvalidInputException;

public interface AmazonS3Service {
	String upload(MultipartFile multipartFile) throws ImageEmptyInvalidInputException, InvalidInputException;

	void deleteFile(String url);
}
