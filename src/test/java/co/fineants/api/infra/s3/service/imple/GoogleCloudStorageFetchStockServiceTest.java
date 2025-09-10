package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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
class GoogleCloudStorageFetchStockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private FetchStockService service;

	@Autowired
	private WriteStockService writeStockService;

	@BeforeEach
	void setUp() {
		Stock samsungStock = TestDataFactory.createSamsungStock();
		Stock kakaoStock = TestDataFactory.createKakaoStock();

		writeStockService.writeStocks(List.of(samsungStock, kakaoStock));
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchStocks() {
		List<Stock> stocks = service.fetchStocks();

		Assertions.assertThat(stocks).hasSize(2);
	}
}
