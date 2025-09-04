package co.fineants.api.infra.s3.service;

import java.io.InputStream;
import java.util.Optional;

public interface RemoteFileFetcher {

	Optional<InputStream> read(String path);
}
