package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3DeleteProfileImageFileServiceTest {

	@Test
	void canCreated() {
		DeleteProfileImageFileService service = new AmazonS3DeleteProfileImageFileService();

		Assertions.assertThat(service).isNotNull();
	}

}
