package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.infra.s3.service.WriteProfileImageFileService;

class GoogleCloudStorageWriteProfileImageFileServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WriteProfileImageFileService service;

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}
}
