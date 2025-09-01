package co.fineants.api.infra.s3.service;

import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.AmazonS3;

public class AmazonS3DeleteDividendService implements DeleteDividendService {

	private final String bucketName;
	private final String dividendPath;
	private final AmazonS3 amazonS3;

	public AmazonS3DeleteDividendService(
		@Value("${aws.s3.bucket}") String bucketName,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath,
		AmazonS3 amazonS3) {
		this.bucketName = bucketName;
		this.dividendPath = dividendPath;
		this.amazonS3 = amazonS3;
	}

	@Override
	public void delete() {
		amazonS3.deleteObject(bucketName, dividendPath);
	}
}
