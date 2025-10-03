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
import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.domain.stock.parser.StockCsvParser;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.DeleteStockService;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteStockService;

class StockDividendSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private StockDividendSetupDataLoader loader;

	@Autowired
	private StockDividendRepository repository;

	@Autowired
	private StockSetupDataLoader stockLoader;

	@Autowired
	private StockCsvParser stockCsvParser;

	@Autowired
	private WriteStockService writeStockService;

	@Autowired
	private StockDividendCsvParser stockDividendCsvParser;

	@Autowired
	private WriteDividendService writeDividendService;

	@Autowired
	private DeleteStockService deleteStockService;

	@Autowired
	private DeleteDividendService deleteDividendService;

	@BeforeEach
	void setUp() throws IOException {
		InputStream inputStream = new ClassPathResource("stocks.csv").getInputStream();
		List<Stock> stocks = stockCsvParser.parse(inputStream);
		writeStockService.writeStocks(stocks);

		inputStream = new ClassPathResource("dividends.csv").getInputStream();
		List<StockDividendTemp> dividends = stockDividendCsvParser.parse(inputStream);
		writeDividendService.writeDividendTemp(dividends.toArray(new StockDividendTemp[0]));

		stockLoader.setupStocks();
	}

	@AfterEach
	void tearDown() {
		deleteStockService.delete();
		deleteDividendService.delete();
	}

	@Test
	void setupStockDividends() {
		loader.setupStockDividends();

		Assertions.assertThat(repository.findAll()).hasSize(326);
	}

	@Test
	void setupStockDividends_whenCalledTwice_thenNoDuplicateEntries() {
		loader.setupStockDividends();
		int initialSize = repository.findAll().size();

		loader.setupStockDividends();
		int newSize = repository.findAll().size();

		Assertions.assertThat(initialSize).isEqualTo(326);
		Assertions.assertThat(newSize)
			.isEqualTo(326)
			.isEqualTo(initialSize);
	}
}
