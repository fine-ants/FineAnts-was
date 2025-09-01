package co.fineants.api.infra.s3.service;

import java.io.File;

public interface FileFetcher {

	File read(String filePath);
}
