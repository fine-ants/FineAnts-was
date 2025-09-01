package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AmazonS3WriteDividendServiceTest {

	private WriteDividendService service;

	@BeforeEach
	void setUp() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();
		RemoteFileUploader fileUploader = Mockito.mock(RemoteFileUploader.class);
		String filePath = "local/dividend/dividends.csv";
		service = new AmazonS3WriteDividendService(formatter, fileUploader, filePath);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend_whenDividendIsEmpty() {
		Assertions.assertThatCode(service::writeDividend).doesNotThrowAnyException();
	}
}
