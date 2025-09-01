package co.fineants.api.infra.s3.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class AmazonS3FileFetcherTest {

	@Test
	void canCreated() {
		FileFetcher fileReader = new AmazonS3FileFetcher();
		Assertions.assertThat(fileReader).isNotNull();
	}
}
