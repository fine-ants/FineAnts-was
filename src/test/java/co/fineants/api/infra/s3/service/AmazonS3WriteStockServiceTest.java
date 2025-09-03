package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AmazonS3WriteStockServiceTest {

	@Test
	void canCreated() {
		RemoteFileUploader uploader = Mockito.mock(RemoteFileUploader.class);
		String filePath = "local/stock/stocks.csv";
		StockCsvFormatter formatter = new StockCsvFormatter();
		WriteStockService service = new AmazonS3WriteStockService(uploader, filePath, formatter);

		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeStocks() {
		RemoteFileUploader uploader = Mockito.mock(RemoteFileUploader.class);
		String filePath = "local/stock/stocks.csv";
		StockCsvFormatter formatter = new StockCsvFormatter();
		WriteStockService service = new AmazonS3WriteStockService(uploader, filePath, formatter);

		Assertions.assertThatCode(() -> service.writeStocks(List.of())).doesNotThrowAnyException();
	}
}
