package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import co.fineants.api.domain.stock.domain.entity.Stock;

class AmazonS3WriteStockServiceTest {

	private WriteStockService service;

	@BeforeEach
	void setUp() {
		RemoteFileUploader uploader = Mockito.mock(RemoteFileUploader.class);
		String filePath = "local/stock/stocks.csv";
		String delimiter = "$";
		String[] headers = {"stockCode", "tickerSymbol", "companyName", "companyNameEng", "sector", "market"};
		CsvFormatter<Stock> formatter = new CsvFormatter<>(delimiter, headers);
		service = new AmazonS3WriteStockService(uploader, filePath, formatter);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeStocks() {
		Assertions.assertThatCode(() -> service.writeStocks(List.of())).doesNotThrowAnyException();
	}
}
