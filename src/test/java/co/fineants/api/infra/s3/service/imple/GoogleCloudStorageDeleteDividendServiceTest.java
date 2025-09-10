package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import co.fineants.api.infra.s3.service.DeleteDividendService;

class GoogleCloudStorageDeleteDividendServiceTest {

	@Test
	void canCreated() {
		DeleteDividendService service = new GoogleCloudStorageDeleteDividendService();

		Assertions.assertThat(service).isNotNull();
	}

}
