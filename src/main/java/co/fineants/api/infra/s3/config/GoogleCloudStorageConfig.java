package co.fineants.api.infra.s3.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.DeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageDeleteDividendService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageDeleteProfileImageFileService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageFetchDividendService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageFetchStockService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageRemoteFileFetcher;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageRemoteFileUploader;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageWriteDividendService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageWriteProfileImageFileService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageWriteStockService;

@Configuration
@Profile(value = {"gcp"})
public class GoogleCloudStorageConfig {

	@Value("${gcp.storage.bucket}")
	private String bucketName;

	@Value("${gcp.project-id}")
	private String projectId;

	@Value("${gcp.credentials}")
	private Resource credentials;
	
	@Bean
	public Storage storage() throws IOException {
		GoogleCredentials googleCredentials = GoogleCredentials.fromStream(this.credentials.getInputStream());

		return StorageOptions.newBuilder()
			.setProjectId(projectId)
			.setCredentials(googleCredentials)
			.build()
			.getService();
	}

	@Bean
	public WriteDividendService writeDividendService(
		CsvFormatter<StockDividend> formatter,
		RemoteFileUploader uploader,
		@Value("${gcp.storage.dividend-csv-path}") String dividendPath) {
		return new GoogleCloudStorageWriteDividendService(formatter, uploader, dividendPath);
	}

	@Bean
	public RemoteFileUploader remoteFileUploader(Storage storage) {
		return new GoogleCloudStorageRemoteFileUploader(storage, bucketName);
	}

	@Bean
	public RemoteFileFetcher remoteFileFetcher(Storage storage) {
		return new GoogleCloudStorageRemoteFileFetcher(storage, bucketName);
	}

	@Bean
	public FetchDividendService fetchDividendService(RemoteFileFetcher fileFetcher,
		@Value("${gcp.storage.dividend-csv-path}") String dividendPath) {
		return new GoogleCloudStorageFetchDividendService(fileFetcher, dividendPath);
	}

	@Bean
	public DeleteDividendService deleteDividendService() {
		return new GoogleCloudStorageDeleteDividendService();
	}

	@Bean
	public DeleteProfileImageFileService deleteProfileImageFileService() {
		return new GoogleCloudStorageDeleteProfileImageFileService();
	}

	@Bean
	public FetchStockService fetchStockService() {
		return new GoogleCloudStorageFetchStockService();
	}

	@Bean
	public WriteProfileImageFileService writeProfileImageFileService() {
		return new GoogleCloudStorageWriteProfileImageFileService();
	}

	@Bean
	public WriteStockService writeStockService() {
		return new GoogleCloudStorageWriteStockService();
	}
}
