package co.fineants.api.infra.s3.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.AmazonS3;

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
	public InputStream read(String path) {
		return amazonS3.getObject(bucketName, path).getObjectContent();
	}
}
