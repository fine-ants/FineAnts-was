package co.fineants.api.infra.s3.service;

import java.io.File;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3FetchDividendServiceTest {
	@Test
	void canCreated() {
		FetchDividendService service = new AmazonS3FetchDividendService();
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividend() {
		FetchDividendService service = new AmazonS3FetchDividendService();

		File file = service.fetchDividend();

		Assertions.assertThat(file).isNotNull();
	}
}
