package co.fineants.api.domain.stock.repository;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;

@Transactional
class StockRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private StockRepository stockRepository;

	@DisplayName("배당금 정보가 없어도 종목 정보 존재할때 종목을 조회한다")
	@Test
	void findAllWithDividends() {
		// given
		stockRepository.save(createSamsungStock());
		List<String> tickerSymbols = List.of("005930");
		// when
		List<Stock> actual = stockRepository.findAllWithDividends(tickerSymbols);
		// then
		Assertions.assertThat(actual).hasSize(1);
	}

	@DisplayName("종목과 배당금을 같이 저장한다")
	@Test
	void shouldSaveStockDividend() {
		// given
		Stock stock = TestDataFactory.createSamsungStock();
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2023, 5, 17);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		StockDividendTemp stockDividendTemp = new StockDividendTemp(
			dividend,
			dividendDates,
			false,
			stock.getTickerSymbol()
		);
		stock.addStockDividendTemp(stockDividendTemp);
		// when
		stockRepository.save(stock);
		// then
		Stock findStock = stockRepository.findByTickerSymbol(stock.getTickerSymbol()).orElseThrow();
		Assertions.assertThat(findStock.getStockDividendTemps())
			.hasSize(1)
			.containsExactlyInAnyOrder(stockDividendTemp);
	}

	@DisplayName("종목의 배당금을 삭제한다")
	@Test
	void shouldDeleteStockDividend() {
		// given
		Stock stock = TestDataFactory.createSamsungStock();
		Money dividend = Money.won(361);
		LocalDate recordDate = LocalDate.of(2023, 3, 31);
		LocalDate exDividendDate = LocalDate.of(2023, 3, 30);
		LocalDate paymentDate = LocalDate.of(2023, 5, 17);
		DividendDates dividendDates = DividendDates.of(recordDate, exDividendDate, paymentDate);
		StockDividendTemp stockDividendTemp = new StockDividendTemp(
			dividend,
			dividendDates,
			false,
			stock.getTickerSymbol()
		);
		stock.addStockDividendTemp(stockDividendTemp);
		stockRepository.save(stock);

		// when
		Stock findStock = stockRepository.findByTickerSymbol(stock.getTickerSymbol()).orElseThrow();
		findStock.removeStockDividendTemp(stockDividendTemp);
		stockRepository.save(findStock);

		// then
		Stock updatedStock = stockRepository.findByTickerSymbol(stock.getTickerSymbol()).orElseThrow();
		Assertions.assertThat(updatedStock.getStockDividendTemps()).isEmpty();
	}
}
