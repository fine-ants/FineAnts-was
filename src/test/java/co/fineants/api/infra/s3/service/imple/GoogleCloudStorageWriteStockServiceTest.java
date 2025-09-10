package co.fineants.api.infra.s3.service.imple;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import co.fineants.api.infra.s3.service.WriteStockService;

class GoogleCloudStorageWriteStockServiceTest {

	@Test
	void canCreated() {
		WriteStockService writeStockService = new GoogleCloudStorageWriteStockService();

		Assertions.assertThat(writeStockService).isNotNull();
	}

}
