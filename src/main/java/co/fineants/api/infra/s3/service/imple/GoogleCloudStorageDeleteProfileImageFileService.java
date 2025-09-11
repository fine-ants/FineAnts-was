package co.fineants.api.infra.s3.service.imple;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.cloud.storage.Storage;

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
		String path = parseProfilePath(url);
		boolean delete = storage.delete(bucketName, path);
		log.info("File deleted from bucket {} with path {}. Success: {}", bucketName, path, delete);
	}

	private String parseProfilePath(String url) {
		try {
			URI uri = new URI(url);
			String fullPath = uri.getPath();
			return fullPath.substring(fullPath.indexOf("/", 1) + 1);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Invalid URL: " + url, e);
		}
	}
}
