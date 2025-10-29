package co.fineants.stock.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;

class FindStockTest extends AbstractContainerBaseTest {

	@Autowired
	private FindStock findStock;

	@Autowired
	private StockRepository stockRepository;

	@DisplayName("삼성 종목 엔티티를 조회한다")
	@Test
	void byTickerSymbol() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		String tickerSymbol = "005930";
		// when
		Stock stock = findStock.byTickerSymbol(tickerSymbol);
		// then
		Assertions.assertThat(stock).isNotNull();
	}

	@DisplayName("종목을 찾지 못하면 예외가 발생한다")
	@Test
	void byTickerSymbol_whenNotExistStock_thenThrowException() {
		// given
		String tickerSymbol = "005930";
		// when
		Throwable throwable = Assertions.catchThrowable(() -> findStock.byTickerSymbol(tickerSymbol));
		// then
		Assertions.assertThat(throwable)
			.isInstanceOf(StockNotFoundException.class);
	}
}
