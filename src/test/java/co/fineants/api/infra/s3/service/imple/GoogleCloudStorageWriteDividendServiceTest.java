package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.time.LocalDate;

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
	void writeDividend_whenDataIsMultiple() {
		Stock stock = TestDataFactory.createSamsungStock();
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2023, 5, 17);
		StockDividend stockDividend = StockDividend.create(
			1L,
			dividend,
			recordDate,
			exDividendDate,
			paymentDate,
			stock
		);

		service.writeDividend(stockDividend);

		InputStream inputStream = fetcher.read(dividendPath).orElseThrow();
		Assertions.assertThat(inputStream).isNotNull();
		FileContentComparator.compare(inputStream, "src/test/resources/gold_dividends.csv");
	}
}
