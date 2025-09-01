package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3DeleteDividendServiceTest {

	@Test
	void canCreated() {
		DeleteDividendService service = new AmazonS3DeleteDividendService();
		Assertions.assertThat(service).isNotNull();
	}

}
