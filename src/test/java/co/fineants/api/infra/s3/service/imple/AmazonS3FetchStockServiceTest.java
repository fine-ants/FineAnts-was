package co.fineants.api.infra.s3.service.imple;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.stock.application.StockCsvLineParser;
import co.fineants.stock.application.StockCsvParser;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.RemoteFileFetcher;
import co.fineants.stock.domain.Stock;

class AmazonS3FetchStockServiceTest {

	private FetchStockService service;

	@BeforeEach
	void setUp() {
		String filePath = "local/stock/stocks.csv";
		RemoteFileFetcher fetcher = Mockito.mock(RemoteFileFetcher.class);
		BDDMockito.given(fetcher.read(filePath))
			.willReturn(Optional.of(getMockInputStream()));
		StockCsvLineParser parser = new StockCsvLineParser("TS");
		StockCsvParser stockCsvParser = new StockCsvParser("\\$", parser);
		service = new AmazonS3FetchStockService(fetcher, filePath, stockCsvParser);
	}

	private InputStream getMockInputStream() {
		try {
			return new java.io.FileInputStream("src/test/resources/stocks.csv");
		} catch (java.io.FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchStocks() {
		List<Stock> stocks = service.fetchStocks();

		Assertions.assertThat(stocks).hasSize(2802);
	}
}
