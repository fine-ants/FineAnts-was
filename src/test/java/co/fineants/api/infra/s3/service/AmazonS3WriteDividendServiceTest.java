package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3WriteDividendServiceTest {

	@Test
	void canCreated() {
		WriteDividendService service = new AmazonS3WriteDividendService();
		Assertions.assertThat(service).isNotNull();
	}
}
