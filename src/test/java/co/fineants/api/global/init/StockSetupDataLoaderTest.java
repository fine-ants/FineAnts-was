package co.fineants.api.global.init;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.repository.StockRepository;

class StockSetupDataLoaderTest extends AbstractContainerBaseTest {

	@Autowired
	private StockSetupDataLoader loader;

	@Autowired
	private StockRepository stockRepository;

	@Test
	void setupStocks() {
		loader.setupStocks();

		Assertions.assertThat(stockRepository.findAll()).hasSizeGreaterThan(2802);
	}

}
