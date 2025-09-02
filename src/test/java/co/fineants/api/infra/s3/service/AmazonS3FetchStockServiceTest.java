package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import co.fineants.api.domain.stock.domain.entity.Stock;

class AmazonS3FetchStockServiceTest {

	@Test
	void canCreated() {
		FetchStockService service = new AmazonS3FetchStockService();

		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchStocks() {
		FetchStockService service = new AmazonS3FetchStockService();

		List<Stock> stocks = service.fetchStocks();
		
		Assertions.assertThat(stocks).isNotNull();
	}
}
