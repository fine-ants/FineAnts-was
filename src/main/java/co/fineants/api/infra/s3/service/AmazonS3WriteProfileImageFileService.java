package co.fineants.api.infra.s3.service;

import org.jetbrains.annotations.NotNull;
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
		String key = generateKey(profileImageFile.getFileName());
		try {
			return uploader.uploadImageFile(profileImageFile, key);
		} catch (Exception e) {
			throw new IllegalArgumentException("can not upload file to s3", e);
		} finally {
			profileImageFile.deleteFile();
		}
	}

	@NotNull
	private String generateKey(String fileName) {
		return profilePath + uuidGenerator.generate() + fileName;
	}
}
