package co.fineants.api.infra.s3.service;

public interface AmazonS3Service {

	void deleteProfileImageFile(String url);
}
