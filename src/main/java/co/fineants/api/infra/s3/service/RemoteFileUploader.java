package co.fineants.api.infra.s3.service;

public interface RemoteFileUploader {
	void upload(String fileContent, String filePath);
}
