package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;

class AmazonS3WriteDividendServiceTest {

	private WriteDividendService service;

	@BeforeEach
	void setUp() {
		CsvFormatter<StockDividend> formatter = new CsvFormatter<>(",",
			new String[] {"id", "dividend", "recordDate", "paymentDate", "stockCode"});
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
