package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;

class GoogleCloudStorageWriteDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private CsvFormatter<StockDividend> formatter;

	@Autowired
	private RemoteFileUploader uploader;

	@Value("${gcp.storage.dividend-csv-path}")
	private String dividendPath;

	private WriteDividendService service;

	@Autowired
	private RemoteFileFetcher fetcher;

	@BeforeEach
	void setUp() {
		service = new GoogleCloudStorageWriteDividendService(formatter, uploader, dividendPath);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend() {
		service.writeDividend();

		Assertions.assertThat(fetcher.read(dividendPath)).isPresent();
	}
}
