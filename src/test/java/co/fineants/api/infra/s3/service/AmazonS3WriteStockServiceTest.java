package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3WriteStockServiceTest {

	@Test
	void canCreated() {
		WriteStockService service = new AmazonS3WriteStockService();

		Assertions.assertThat(service).isNotNull();
	}

}
