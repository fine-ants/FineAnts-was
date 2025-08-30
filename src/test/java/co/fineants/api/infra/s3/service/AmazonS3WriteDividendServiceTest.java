package co.fineants.api.infra.s3.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AmazonS3WriteDividendServiceTest {

	@Test
	void canCreated() {
		WriteDividendService service = new AmazonS3WriteDividendService();
		assertNotNull(service);
	}
}
