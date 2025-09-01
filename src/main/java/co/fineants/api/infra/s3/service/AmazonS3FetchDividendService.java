package co.fineants.api.infra.s3.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;

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
	public List<StockDividend> fetchDividend() {
		FileFetcher fileReader = new AmazonS3FileFetcher();
		return Collections.emptyList();
	}
}
