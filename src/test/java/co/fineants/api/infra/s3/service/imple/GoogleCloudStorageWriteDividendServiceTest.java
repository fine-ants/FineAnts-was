package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles(value = {"test", "gcp"}, inheritProfiles = false)
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageWriteDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private RemoteFileFetcher fetcher;

	@Autowired
	private WriteDividendService service;

	@Value("${gcp.storage.dividend-csv-path}")
	private String dividendPath;

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
		Assertions.assertThat(service).isInstanceOf(GoogleCloudStorageWriteDividendService.class);
	}

	@Test
	void writeDividend() {
		service.writeDividend();

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(service).isInstanceOf(GoogleCloudStorageWriteDividendService.class);
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void writeDividend_whenDataIsOne() {
		Stock stock = TestDataFactory.createSamsungStock();
		StockDividend stockDividend = TestDataFactory.createSamsungStockDividend(stock);

		service.writeDividend(List.of(stockDividend));

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends.csv");
	}

	@Test
	void writeDividend_whenDataIsTwo() {
		Stock stock = TestDataFactory.createSamsungStock();
		StockDividend stockDividend = TestDataFactory.createSamsungStockDividend(stock);

		Stock kakaoStock = TestDataFactory.createKakaoStock();
		StockDividend stockDividend2 = TestDataFactory.createKakaoStockDividend(kakaoStock);

		service.writeDividend(List.of(stockDividend, stockDividend2));

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends_2.csv");
	}
}
