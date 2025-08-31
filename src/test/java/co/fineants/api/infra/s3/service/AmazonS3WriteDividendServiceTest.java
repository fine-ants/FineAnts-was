package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3WriteDividendServiceTest {

	@Test
	void canCreated() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();
		WriteDividendService service = new AmazonS3WriteDividendService(formatter);
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend_whenDividendIsEmpty() {
		DividendCsvFormatter formatter = new DividendCsvFormatter();
		WriteDividendService service = new AmazonS3WriteDividendService(formatter);

		Assertions.assertThatCode(service::writeDividend).doesNotThrowAnyException();
	}
}
