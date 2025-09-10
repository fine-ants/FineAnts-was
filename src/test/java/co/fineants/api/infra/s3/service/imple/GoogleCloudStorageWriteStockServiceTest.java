package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageWriteStockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private FetchStockService fetchStockService;

	@Test
	void canCreated() {
		WriteStockService writeStockService = new GoogleCloudStorageWriteStockService();

		Assertions.assertThat(writeStockService).isNotNull();
	}

	@Test
	void writeStocks_whenStocksIsEmpty() {
		WriteStockService writeStockService = new GoogleCloudStorageWriteStockService();

		writeStockService.writeStocks(List.of());

		List<Stock> stocks = fetchStockService.fetchStocks();
		Assertions.assertThat(stocks).isEmpty();
	}
}
