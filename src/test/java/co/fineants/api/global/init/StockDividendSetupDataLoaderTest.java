package co.fineants.api.global.init;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;

class StockDividendSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private StockDividendSetupDataLoader loader;

	@Autowired
	private StockDividendRepository repository;

	@Autowired
	private StockSetupDataLoader stockLoader;

	@BeforeEach
	void setUp() {
		stockLoader.setupStocks();
	}

	@Test
	void setupStockDividends() {
		loader.setupStockDividends();

		Assertions.assertThat(repository.findAll()).hasSizeGreaterThan(0);
	}

	@Test
	void setupStockDividends_whenCalledTwice_thenNoDuplicateEntries() {
		loader.setupStockDividends();
		int initialSize = repository.findAll().size();

		loader.setupStockDividends();
		int newSize = repository.findAll().size();

		Assertions.assertThat(newSize).isEqualTo(initialSize);
	}
}
