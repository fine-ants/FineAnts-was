package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageWriteStockServiceTest extends AbstractContainerBaseTest {
	@Autowired
	private WriteStockService service;

	@Autowired
	private FetchStockService fetchStockService;

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeStocks_whenStocksIsEmpty() {
		service.writeStocks(List.of());

		List<Stock> stocks = fetchStockService.fetchStocks();
		Assertions.assertThat(stocks).isEmpty();
	}

	@Test
	void writeStocks() {
		Stock samsungStock = TestDataFactory.createSamsungStock();
		Stock kakaoStock = TestDataFactory.createKakaoStock();

		service.writeStocks(List.of(samsungStock, kakaoStock));

		List<Stock> stocks = fetchStockService.fetchStocks();
		Assertions.assertThat(stocks).hasSize(2);
	}
}
