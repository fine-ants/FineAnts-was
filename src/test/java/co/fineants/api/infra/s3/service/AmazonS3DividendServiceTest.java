package co.fineants.api.infra.s3.service;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;

class AmazonS3DividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private AmazonS3DividendService service;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private WriteDividendService writeDividendService;

	@DisplayName("배당금 데이터를 읽어온다")
	@Test
	void fetchDividend() {
		// given
		Stock samsung = stockRepository.save(createSamsungStock());
		List<StockDividend> stockDividends = stockDividendRepository.saveAll(createStockDividendWith(samsung));
		writeDividendService.writeDividend(stockDividends);
		// when
		List<StockDividend> dividends = service.fetchDividends();
		// then
		Assertions.assertThat(dividends).hasSize(7);
	}
}
