package co.fineants.api.infra.s3.service.imple;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;

import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCloudStorageDeleteProfileImageFileService implements DeleteProfileImageFileService {

	private final Storage storage;
	private final String bucketName;

	public GoogleCloudStorageDeleteProfileImageFileService(Storage storage, String bucketName) {
		this.storage = storage;
		this.bucketName = bucketName;
	}

	@Override
	public void delete(String url) {
		String path;
		try {
			path = parseProfilePath(url);
		} catch (Exception e) {
			log.warn("Failed to parse profile image URL: {}", url, e);
			return;
		}

		boolean delete;
		try {
			delete = storage.delete(bucketName, path);
		} catch (StorageException e) {
			log.warn("Failed to delete file from bucket {} with path {}", bucketName, path, e);
			return;
		}
		log.info("File deleted from bucket {} with path {}. Success: {}", bucketName, path, delete);
	}

	private String parseProfilePath(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String fullPath = uri.getPath();
		return fullPath.substring(fullPath.indexOf("/", 1) + 1);
	}
}
