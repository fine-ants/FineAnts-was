package co.fineants.api.infra.s3.service.imple;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.WriteDividendService;

class AmazonS3WriteDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private WriteDividendService service;

	@Autowired
	private FetchDividendService fetchDividendService;

	@Autowired
	private StockRepository stockRepository;

	@BeforeEach
	void setUp() {
		Stock samsung = TestDataFactory.createSamsungStock();
		stockRepository.save(samsung);
	}

	@Test
	void canCreated() {
		Assertions.assertThat(service).isNotNull();
	}

	@Test
	void writeDividend_whenDividendIsEmpty() {
		Assertions.assertThatCode(service::writeDividendTemp).doesNotThrowAnyException();
	}

	@Test
	void writeDividend() {
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 6, 30);
		LocalDate exDividendDate = LocalDate.of(2023, 6, 29);
		LocalDate paymentDate = LocalDate.of(2023, 7, 21);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		boolean isDeleted = false;
		String tickerSymbol = "005930";
		StockDividendTemp stockDividendTemp = new StockDividendTemp(
			dividend,
			dividendDates,
			isDeleted,
			tickerSymbol
		);

		service.writeDividendTemp(stockDividendTemp);

		Stock findStock = stockRepository.findByTickerSymbol(tickerSymbol).orElseThrow();
		List<StockDividendTemp> actual = fetchDividendService.fetchDividendEntityIn(List.of(findStock));
		List<StockDividendTemp> expected = List.of(stockDividendTemp);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
