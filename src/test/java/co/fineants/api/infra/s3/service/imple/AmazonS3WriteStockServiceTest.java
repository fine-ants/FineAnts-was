package co.fineants.api.infra.s3.service.imple;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.stock.domain.Stock;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.api.infra.s3.service.WriteStockService;

class AmazonS3WriteStockServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WriteStockService service;

	@Autowired
	private FetchStockService fetchStockService;

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeStocks_whenStocksIsEmpty() {
		List<Stock> stocks = new ArrayList<>();

		service.writeStocks(stocks);

		List<Stock> findStocks = fetchStockService.fetchStocks();
		Assertions.assertThat(findStocks).isEmpty();
	}

	@Test
	void writeStocks_whenStockIsMultiple() {
		List<Stock> stocks = List.of(TestDataFactory.createSamsungStock(), TestDataFactory.createDongwhaPharmStock());

		service.writeStocks(stocks);

		List<Stock> findStocks = fetchStockService.fetchStocks();
		Assertions.assertThat(findStocks).containsExactlyElementsOf(stocks);
	}
}
