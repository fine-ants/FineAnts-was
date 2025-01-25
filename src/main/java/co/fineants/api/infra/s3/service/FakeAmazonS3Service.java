package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.global.errors.exception.BadRequestException;

public class FakeAmazonS3Service implements AmazonS3Service {

	@Override
	public String upload(MultipartFile multipartFile) throws BadRequestException {
		return "profileUrl";
	}

	@Override
	public void deleteFile(String url) {
		// this service not delete the file.
	}
}
