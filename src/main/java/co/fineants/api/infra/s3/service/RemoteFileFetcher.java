package co.fineants.api.infra.s3.service;

import java.io.InputStream;

public interface RemoteFileFetcher {

	InputStream read(String path);
}
