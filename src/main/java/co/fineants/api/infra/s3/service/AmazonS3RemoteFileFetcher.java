package co.fineants.api.infra.s3.service;

import java.io.InputStream;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;

@Component
public class AmazonS3RemoteFileFetcher implements RemoteFileFetcher {

	private final String bucketName;
	private final AmazonS3 amazonS3;

	public AmazonS3RemoteFileFetcher(
		@Value("${aws.s3.bucket}") String bucketName,
		AmazonS3 amazonS3) {
		this.bucketName = bucketName;
		this.amazonS3 = amazonS3;
	}

	@Override
	public Optional<InputStream> read(String path) {
		try {
			return Optional.ofNullable(amazonS3.getObject(bucketName, path).getObjectContent());
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
