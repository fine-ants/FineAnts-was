package co.fineants.api.infra.s3.service;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.AmazonS3;

public class AmazonS3FetchDividendService implements FetchDividendService {

	private final String bucketName;
	private final String dividendPath;
	private final AmazonS3 amazonS3;

	public AmazonS3FetchDividendService(
		@Value("${aws.s3.bucket}") String bucketName,
		@Value("${aws.s3.dividend-csv-path}") String dividendPath,
		AmazonS3 amazonS3) {
		this.bucketName = bucketName;
		this.dividendPath = dividendPath;
		this.amazonS3 = amazonS3;
	}

	@Override
	public File fetchDividend() {
		return new File("src/test/resources/gold_dividends.csv");
	}
}
