package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

public interface WriteProfileImageFileService {

	String upload(MultipartFile multipartFile);
}
