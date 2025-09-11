package co.fineants.api.infra.s3.service.imple;

import com.amazonaws.services.s3.AmazonS3;

import co.fineants.api.infra.s3.service.DeleteStockService;

public class AmazonS3DeleteStockService implements DeleteStockService {
	private final AmazonS3 amazonS3;
	private final String bucketName;
	private final String dividendPath;

	public AmazonS3DeleteStockService(AmazonS3 amazonS3, String bucketName, String dividendPath) {
		this.amazonS3 = amazonS3;
		this.bucketName = bucketName;
		this.dividendPath = dividendPath;
	}

	@Override
	public void delete() {
		amazonS3.deleteObject(bucketName, dividendPath);
	}
}
