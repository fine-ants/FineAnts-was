package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.temp.InvalidInputException;

public interface AmazonS3Service {
	String upload(MultipartFile multipartFile) throws InvalidInputException;

	void deleteFile(String url);
}
