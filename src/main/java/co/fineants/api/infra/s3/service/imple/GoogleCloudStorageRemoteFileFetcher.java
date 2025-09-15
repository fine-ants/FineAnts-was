package co.fineants.api.infra.s3.service.imple;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;

import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GoogleCloudStorageRemoteFileFetcher implements RemoteFileFetcher {

	private final Storage storage;
	private final String bucketName;

	public GoogleCloudStorageRemoteFileFetcher(Storage storage, String bucketName) {
		this.storage = storage;
		this.bucketName = bucketName;
	}

	@Override
	public Optional<InputStream> read(String path) {
		Optional<Blob> blob;
		try {
			blob = Optional.ofNullable(storage.get(bucketName, path));
		} catch (Exception e) {
			log.warn("Failed to get blob from path: {}", path, e);
			return Optional.empty();
		}

		try {
			return blob.map(Blob::getContent)
				.map(ByteArrayInputStream::new);
		} catch (StorageException e) {
			log.warn("Failed to read file from path: {}", path, e);
			return Optional.empty();
		}
	}
}
