package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.Optional;

import co.fineants.api.infra.s3.service.RemoteFileFetcher;

public class GoogleCloudStorageRemoteFileFetcher implements RemoteFileFetcher {
	@Override
	public Optional<InputStream> read(String path) {
		return Optional.empty();
	}
}
