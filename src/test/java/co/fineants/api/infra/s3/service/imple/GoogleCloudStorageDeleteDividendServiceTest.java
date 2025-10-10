package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.infra.s3.dto.StockDividendDto;
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
		StockDividendTemp stockDividend = TestDataFactory.createSamsungStockDividendTemp();
		StockDividendTemp stockDividend2 = TestDataFactory.createKakaoStockDividend();

		writeDividendService.writeDividendTemp(stockDividend, stockDividend2);
	}

	@AfterEach
	void tearDown() {
		service.delete();
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void delete() {
		service.delete();

		List<StockDividendDto> list = fetchDividendService.fetchDividend();

		Assertions.assertThat(list).isEmpty();
	}

	@Test
	void delete_whenFileNotExists_thenNotThrowAnyException() {
		service.delete();

		Assertions.assertThatCode(() -> service.delete()).doesNotThrowAnyException();
	}

}
