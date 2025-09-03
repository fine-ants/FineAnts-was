package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

public interface RemoteFileUploader {
	void upload(String fileContent, String filePath);

	String uploadImageFile(MultipartFile multipartFile, String filePath);
}
