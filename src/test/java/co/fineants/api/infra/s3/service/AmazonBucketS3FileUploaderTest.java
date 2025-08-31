package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonBucketS3FileUploaderTest {

	@Test
	void canCreated() {
		FileUploader fileUploader = new AmazonBucketS3FileUploader();

		Assertions.assertThat(fileUploader).isNotNull();
	}

}
