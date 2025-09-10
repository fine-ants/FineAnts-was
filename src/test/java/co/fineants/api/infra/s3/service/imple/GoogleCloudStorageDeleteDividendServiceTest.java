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
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageDeleteDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private DeleteDividendService service;

	@Autowired
	private WriteDividendService writeDividendService;

	@Autowired
	private FetchDividendService fetchDividendService;

	@BeforeEach
	void setUp() {
		Stock stock = TestDataFactory.createSamsungStock();
		StockDividend stockDividend = TestDataFactory.createSamsungStockDividend(stock);

		Stock kakaoStock = TestDataFactory.createKakaoStock();
		StockDividend stockDividend2 = TestDataFactory.createKakaoStockDividend(kakaoStock);

		writeDividendService.writeDividend(List.of(stockDividend, stockDividend2));
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void delete() {
		service.delete();

		Throwable throwable = Assertions.catchThrowable(() -> fetchDividendService.fetchDividend());
		Assertions.assertThat(throwable)
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Failed to read dividend file from Google Storage");
	}

}
