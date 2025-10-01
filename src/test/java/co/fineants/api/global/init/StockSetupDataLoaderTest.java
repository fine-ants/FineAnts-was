package co.fineants.api.global.init;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.parser.StockCsvParser;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.infra.s3.service.DeleteStockService;
import co.fineants.api.infra.s3.service.WriteStockService;

class StockSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private StockSetupDataLoader loader;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private WriteStockService writeStockService;

	@Autowired
	private StockCsvParser stockCsvParser;

	@Autowired
	private DeleteStockService deleteStockService;

	@BeforeEach
	void setUp() throws IOException {
		InputStream inputStream = new ClassPathResource("stocks.csv").getInputStream();
		List<Stock> stocks = stockCsvParser.parse(inputStream);
		writeStockService.writeStocks(stocks);
	}

	@AfterEach
	void tearDown() {
		deleteStockService.delete();
	}

	@Test
	void setupStocks() {
		loader.setupStocks();

		Assertions.assertThat(stockRepository.findAll()).hasSize(2802);
	}

	@Test
	void setupStocks_whenCalledTwice_thenNoDuplicateEntries() {
		loader.setupStocks();
		int initialSize = stockRepository.findAll().size();

		loader.setupStocks();
		int newSize = stockRepository.findAll().size();

		Assertions.assertThat(newSize).isEqualTo(initialSize);
	}
}
