package co.fineants.api.infra.s3.service.imple;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.global.common.csv.CsvFormatter;
import co.fineants.api.infra.s3.service.RemoteFileUploader;
import co.fineants.api.infra.s3.service.WriteDividendService;

class AmazonS3WriteDividendServiceTest {

	private WriteDividendService service;

	@BeforeEach
	void setUp() {
		CsvFormatter<StockDividend> formatter = new CsvFormatter<>(",",
			new String[] {"id", "dividend", "recordDate", "paymentDate", "stockCode"});
		RemoteFileUploader fileUploader = Mockito.mock(RemoteFileUploader.class);
		String filePath = "local/dividend/dividends.csv";
		service = new AmazonS3WriteDividendService(formatter, fileUploader, filePath);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend_whenDividendIsEmpty() {
		Assertions.assertThatCode(service::writeDividend).doesNotThrowAnyException();
	}

	@Test
	void writeDividend() {
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 6, 30);
		LocalDate exDividendDate = LocalDate.of(2023, 6, 29);
		LocalDate paymentDate = LocalDate.of(2023, 7, 21);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		boolean isDeleted = false;
		String tickerSymbol = "005930";
		StockDividendTemp stockDividendTemp = new StockDividendTemp(
			dividend,
			dividendDates,
			isDeleted,
			tickerSymbol
		);

		service.writeDividendTemp(stockDividendTemp);
	}
}
