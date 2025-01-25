package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

public interface AmazonS3Service {
	String upload(MultipartFile multipartFile);

	void deleteFile(String url);
}
