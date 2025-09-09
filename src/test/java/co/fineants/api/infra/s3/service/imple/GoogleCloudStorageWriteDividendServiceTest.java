package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.config.GoogleCloudStorageBucketInitializer;
import co.fineants.config.GoogleCloudStorageTestConfig;

@ActiveProfiles("gcp")
@ContextConfiguration(classes = {GoogleCloudStorageTestConfig.class, GoogleCloudStorageBucketInitializer.class})
class GoogleCloudStorageWriteDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private RemoteFileFetcher fetcher;

	@Autowired
	private WriteDividendService service;

	@Value("${gcp.storage.dividend-csv-path}")
	private String dividendPath;

	private StockDividend createSamsungStockDividend(Stock stock) {
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2023, 5, 17);
		return StockDividend.create(
			1L,
			dividend,
			recordDate,
			exDividendDate,
			paymentDate,
			stock
		);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend() {
		service.writeDividend();

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_empty_dividends.csv");
	}

	@Test
	void writeDividend_whenDataIsOne() {
		Stock stock = TestDataFactory.createSamsungStock();
		StockDividend stockDividend = createSamsungStockDividend(stock);

		service.writeDividend(List.of(stockDividend));

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends.csv");
	}

	@Test
	void writeDividend_whenDataIsTwo() {
		Stock stock = TestDataFactory.createSamsungStock();
		StockDividend stockDividend = createSamsungStockDividend(stock);

		Stock kakaoStock = TestDataFactory.createKakaoStock();
		StockDividend stockDividend2 = StockDividend.create(
			2L,
			Money.won(68),
			LocalDate.of(2025, 3, 10),
			LocalDate.of(2025, 3, 7),
			LocalDate.of(2025, 4, 24),
			kakaoStock
		);

		service.writeDividend(List.of(stockDividend, stockDividend2));

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends_2.csv");
	}
}
