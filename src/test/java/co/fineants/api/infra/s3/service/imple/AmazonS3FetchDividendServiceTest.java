package co.fineants.api.infra.s3.service.imple;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.WriteDividendService;

class AmazonS3FetchDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private FetchDividendService service;

	@Autowired
	private WriteDividendService writeDividendService;

	@BeforeEach
	void setUp() {
		StockDividend stockDividend = TestDataFactory.createSamsungStockDividend();
		StockDividend stockDividend2 = TestDataFactory.createKakaoStockDividend();

		writeDividendService.writeDividend(stockDividend, stockDividend2);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void fetchDividendEntity() {
		List<Stock> stocks = List.of(TestDataFactory.createSamsungStock(), TestDataFactory.createKakaoStock());
		List<StockDividend> list = service.fetchDividendEntityIn(stocks);

		Assertions.assertThat(list).hasSize(2);
	}
}
