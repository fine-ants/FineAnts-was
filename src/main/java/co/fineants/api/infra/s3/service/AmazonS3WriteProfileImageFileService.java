package co.fineants.api.infra.s3.service;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.holding.domain.factory.UuidGenerator;

public class AmazonS3WriteProfileImageFileService implements WriteProfileImageFileService {

	private final RemoteFileUploader uploader;
	private final String profilePath;
	private final UuidGenerator uuidGenerator;

	public AmazonS3WriteProfileImageFileService(RemoteFileUploader uploader, String profilePath,
		UuidGenerator uuidGenerator) {
		this.uploader = uploader;
		this.profilePath = profilePath;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public String upload(MultipartFile multipartFile) {
		ProfileImageFile profileImageFile = new ProfileImageFile(multipartFile);
		String key = profilePath + uuidGenerator.generate() + profileImageFile.getFileName();
		String filePath = uploader.uploadImageFile(profileImageFile, key);
		profileImageFile.deleteFile();
		return filePath;
	}
}
