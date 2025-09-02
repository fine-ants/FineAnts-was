package co.fineants.api.infra.s3.service;

import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.parser.StockParser;

class AmazonS3FetchStockServiceTest {

	private FetchStockService service;

	private InputStream getMockInputStream() {
		try {
			return new java.io.FileInputStream("src/test/resources/stocks.csv");
		} catch (java.io.FileNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@BeforeEach
	void setUp() {
		RemoteFileFetcher fetcher = Mockito.mock(RemoteFileFetcher.class);
		BDDMockito.given(fetcher.read("local/stock/stocks.csv"))
			.willReturn(getMockInputStream());
		StockParser parser = new StockParser();
		service = new AmazonS3FetchStockService(fetcher, parser);
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
