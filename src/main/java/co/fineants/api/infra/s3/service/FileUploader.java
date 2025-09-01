package co.fineants.api.infra.s3.service;

public interface FileUploader {
	void upload(String fileName, String fileContent, String filePath);
}
