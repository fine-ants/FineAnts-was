package co.fineants.api.infra.s3.service.imple;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;

import co.fineants.api.infra.s3.service.DeleteDividendService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCloudStorageDeleteDividendService implements DeleteDividendService {

	private final Storage storage;
	private final String bucketName;
	private final String dividendPath;

	public GoogleCloudStorageDeleteDividendService(Storage storage, String bucketName, String dividendPath) {
		this.storage = storage;
		this.bucketName = bucketName;
		this.dividendPath = dividendPath;
	}

	@Override
	public void delete() {
		BlobId blobId = BlobId.of(bucketName, dividendPath);
		boolean delete = storage.delete(blobId);
		log.info("Deleted file at path: {} from bucket: {}. Success: {}", dividendPath, bucketName, delete);
	}
}
