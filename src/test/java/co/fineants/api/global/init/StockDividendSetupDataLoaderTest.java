package co.fineants.api.global.init;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.dividend.domain.parser.StockDividendCsvParser;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.api.domain.stock.parser.StockCsvParser;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.infra.s3.service.DeleteDividendService;
import co.fineants.api.infra.s3.service.DeleteStockService;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteStockService;

class StockDividendSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private StockDividendSetupDataLoader loader;

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

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	void setUp() throws IOException {
		InputStream inputStream = new ClassPathResource("stocks.csv").getInputStream();
		List<Stock> stocks = stockCsvParser.parse(inputStream);
		writeStockService.writeStocks(stocks);

		inputStream = new ClassPathResource("dividends.csv").getInputStream();
		List<StockDividend> dividends = stockDividendCsvParser.parse(inputStream);
		writeDividendService.writeDividend(dividends.toArray(new StockDividend[0]));

		stockLoader.setupStocks();
	}

	@AfterEach
	void tearDown() {
		deleteStockService.delete();
		deleteDividendService.delete();
	}

	@Transactional
	@Test
	void setupStockDividends() {
		loader.setupStockDividends();

		Set<StockDividend> stockDividends = stockRepository.findAll().stream()
			.flatMap(stock -> stock.getStockDividends().stream())
			.collect(Collectors.toUnmodifiableSet());
		Assertions.assertThat(stockDividends).hasSize(3);
	}

	@Transactional
	@Test
	void setupStockDividends_whenCalledTwice_thenNoDuplicateEntries() {
		loader.setupStockDividends();

		int initialSize = stockRepository.findAll().stream()
			.flatMap(stock -> stock.getStockDividends().stream())
			.collect(Collectors.toUnmodifiableSet())
			.size();

		loader.setupStockDividends();
		int newSize = stockRepository.findAll().stream()
			.flatMap(stock -> stock.getStockDividends().stream())
			.collect(Collectors.toUnmodifiableSet())
			.size();

		Assertions.assertThat(initialSize).isEqualTo(3);
		Assertions.assertThat(newSize)
			.isEqualTo(3)
			.isEqualTo(initialSize);
	}
}
