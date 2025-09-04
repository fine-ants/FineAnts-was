package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3WriteProfileImageFileServiceTest {

	@Test
	void canCreated() {
		WriteProfileImageFileService service = new AmazonS3WriteProfileImageFileService();

		Assertions.assertThat(service).isNotNull();
	}

}
