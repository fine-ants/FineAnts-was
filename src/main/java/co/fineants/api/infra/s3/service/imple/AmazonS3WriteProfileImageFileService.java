package co.fineants.api.infra.s3.service.imple;

import org.springframework.web.multipart.MultipartFile;

import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import co.fineants.api.domain.member.domain.entity.ProfileImageFile;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import jakarta.validation.constraints.NotNull;

public class AmazonS3WriteProfileImageFileService implements WriteProfileImageFileService {

	private final RemoteFileUploader uploader;
	private final String profilePath;
	private final UuidGenerator uuidGenerator;

	public AmazonS3WriteProfileImageFileService(
		RemoteFileUploader uploader,
		String profilePath,
		UuidGenerator uuidGenerator) {
		this.uploader = uploader;
		this.profilePath = profilePath;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public String upload(MultipartFile multipartFile) {
		ProfileImageFile profileImageFile = new ProfileImageFile(multipartFile);
		String filePath = generateKey(profileImageFile.getFileName());
		try {
			return uploader.uploadImageFile(profileImageFile, filePath);
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
