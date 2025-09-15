package co.fineants.api.infra.s3.service.imple;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.infra.s3.service.DeleteDividendService;

public class AmazonS3DeleteDividendService implements DeleteDividendService {

	private final String bucketName;
	private final String dividendPath;
	private final AmazonS3 amazonS3;

	public AmazonS3DeleteDividendService(String bucketName, String dividendPath, AmazonS3 amazonS3) {
		this.bucketName = bucketName;
		this.dividendPath = dividendPath;
		this.amazonS3 = amazonS3;
	}

	@Override
	public void delete() {
		amazonS3.deleteObject(bucketName, dividendPath);
	}
}
