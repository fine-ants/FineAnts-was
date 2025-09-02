package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3FetchStockServiceTest {

	@Test
	void canCreated() {
		FetchStockService service = new AmazonS3FetchStockService();

		Assertions.assertThat(service).isNotNull();
	}

}
