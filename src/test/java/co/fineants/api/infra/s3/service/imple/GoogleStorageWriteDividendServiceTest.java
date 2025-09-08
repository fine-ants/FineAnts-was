package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.infra.s3.service.WriteDividendService;

class GoogleStorageWriteDividendServiceTest extends AbstractContainerBaseTest {

	@Test
	void canCreated() {
		WriteDividendService service = new GoogleStorageWriteDividendService();

		Assertions.assertThat(service).isNotNull();
	}
}
