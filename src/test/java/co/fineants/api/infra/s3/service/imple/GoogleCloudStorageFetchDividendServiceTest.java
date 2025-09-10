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
import co.fineants.api.infra.s3.dto.StockDividendDto;
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
	void fetchDividend() {
		List<StockDividendDto> list = service.fetchDividend();

		Assertions.assertThat(list).hasSize(2);
	}
}
