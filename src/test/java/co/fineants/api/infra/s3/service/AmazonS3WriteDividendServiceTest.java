package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;

class AmazonS3WriteDividendServiceTest {

	@Test
	void canCreated() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();
		String bucketName = "fineants2024";
		String dividendPath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);

		WriteDividendService service = new AmazonS3WriteDividendService(formatter, bucketName, dividendPath, amazonS3);

		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend_whenDividendIsEmpty() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();
		String bucketName = "fineants2024";
		String dividendPath = "local/dividend/dividends.csv";
		AmazonS3 amazonS3 = Mockito.mock(AmazonS3.class);
		WriteDividendService service = new AmazonS3WriteDividendService(formatter, bucketName, dividendPath, amazonS3);

		Assertions.assertThatCode(service::writeDividend).doesNotThrowAnyException();
	}
}
