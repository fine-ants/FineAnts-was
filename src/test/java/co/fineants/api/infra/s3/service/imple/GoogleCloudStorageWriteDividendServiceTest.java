package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.WriteDividendService;

// todo: gcp용 uploader, fetcher 구현체 확장
@ActiveProfiles("gcp")
class GoogleCloudStorageWriteDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private RemoteFileFetcher fetcher;

	@Autowired
	private WriteDividendService service;

	@Value("${gcp.storage.dividend-csv-path}")
	private String dividendPath;

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
