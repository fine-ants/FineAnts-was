package co.fineants.api.infra.s3.service;

import static java.nio.charset.StandardCharsets.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import co.fineants.api.domain.holding.domain.factory.UuidGenerator;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AmazonS3RemoteFileUploader implements RemoteFileUploader {

	private final String bucketName;

	private final AmazonS3 amazonS3;
	private UuidGenerator uuidGenerator;

	public AmazonS3RemoteFileUploader(
		@Value("${aws.s3.bucket}") String bucketName,
		AmazonS3 amazonS3,
		UuidGenerator uuidGenerator) {
		this.bucketName = bucketName;
		this.amazonS3 = amazonS3;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public void upload(String fileContent, String filePath) {
		PutObjectRequest request;
		try (InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(UTF_8))) {
			request = new PutObjectRequest(bucketName, filePath, inputStream, createObjectMetadata());
		} catch (IOException e) {
			throw new IllegalStateException("can not create InputStream", e);
		}
		amazonS3.putObject(request);
	}

	@NotNull
	private ObjectMetadata createObjectMetadata() {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("text/csv");
		return metadata;
	}

	// todo: key 생성 부분을 profilePath에 통합
	@Override
	public String uploadImageFile(ProfileImageFile profileImageFile, String profilePath) {
		amazonS3.putObject(new PutObjectRequest(bucketName, profilePath, profileImageFile.getFile())
			.withCannedAcl(CannedAccessControlList.PublicRead));
		// delete object
		try {
			Files.delete(profileImageFile.getFile().toPath());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return profilePath;
	}
}
