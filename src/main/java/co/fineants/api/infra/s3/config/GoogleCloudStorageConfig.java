package co.fineants.api.infra.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.imple.GoogleCloudStorageWriteDividendService;

@Configuration
@Profile(value = {"local", "release", "production", "gcp"})
public class GoogleCloudStorageConfig {
	@Bean
	public WriteDividendService writeDividendService(
		CsvFormatter<StockDividend> formatter,
		RemoteFileUploader uploader,
		@Value("${gcp.storage.dividend-csv-path}") String dividendPath) {
		return new GoogleCloudStorageWriteDividendService(formatter, uploader, dividendPath);
	}
}
