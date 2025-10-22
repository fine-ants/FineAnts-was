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
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageFetchDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WriteDividendService writeDividendService;

	@Autowired
	private FetchDividendService service;

	@BeforeEach
	void setUp() {
		StockDividend stockDividend = TestDataFactory.createSamsungStockDividend();
		StockDividend stockDividend2 = TestDataFactory.createKakaoStockDividend();
		writeDividendService.writeDividend(stockDividend, stockDividend2);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividendEntityIn_whenStockIsEmpty() {
		List<StockDividend> list = service.fetchDividendEntityIn(List.of());

		Assertions.assertThat(list).isEmpty();
	}

	@Test
	void fetchDividendEntityIn() {
		Stock stock = TestDataFactory.createSamsungStock();
		Stock kakaoStock = TestDataFactory.createKakaoStock();

		List<StockDividend> list = service.fetchDividendEntityIn(List.of(stock, kakaoStock));

		Assertions.assertThat(list).hasSize(2);
	}
}
