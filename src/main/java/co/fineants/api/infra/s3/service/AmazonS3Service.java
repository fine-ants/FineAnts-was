package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.BadRequestException;

public interface AmazonS3Service {
	String upload(MultipartFile multipartFile) throws BadRequestException;

	void deleteFile(String url);
}
