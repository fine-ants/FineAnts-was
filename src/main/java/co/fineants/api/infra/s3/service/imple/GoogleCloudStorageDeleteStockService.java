package co.fineants.api.infra.s3.service.imple;

import com.google.cloud.storage.Storage;

import co.fineants.api.infra.s3.service.DeleteStockService;

public class GoogleCloudStorageDeleteStockService implements DeleteStockService {

	private final Storage storage;

	private final String bucketName;
	private final String filePath;

	public GoogleCloudStorageDeleteStockService(Storage storage, String bucketName, String filePath) {
		this.storage = storage;
		this.bucketName = bucketName;
		this.filePath = filePath;
	}

	@Override
	public void delete() {
		storage.delete(bucketName, filePath);
	}
}
