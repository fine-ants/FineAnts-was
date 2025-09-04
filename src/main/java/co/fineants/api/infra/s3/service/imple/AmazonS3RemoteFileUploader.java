package co.fineants.api.infra.s3.service.imple;

import static java.nio.charset.StandardCharsets.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import co.fineants.api.infra.s3.service.RemoteFileUploader;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AmazonS3RemoteFileUploader implements RemoteFileUploader {

	private final String bucketName;

	private final AmazonS3 amazonS3;

	public AmazonS3RemoteFileUploader(
		@Value("${aws.s3.bucket}") String bucketName,
		AmazonS3 amazonS3) {
		this.bucketName = bucketName;
		this.amazonS3 = amazonS3;
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

	@Override
	public String uploadImageFile(ProfileImageFile profileImageFile, String key) {
		PutObjectRequest request = new PutObjectRequest(bucketName, key, profileImageFile.getFile())
			.withCannedAcl(CannedAccessControlList.PublicRead);
		amazonS3.putObject(request);
		return amazonS3.getUrl(bucketName, key).toString();
	}
}
