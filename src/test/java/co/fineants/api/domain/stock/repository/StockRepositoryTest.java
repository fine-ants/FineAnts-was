package co.fineants.api.domain.stock.repository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.domain.entity.Stock;

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
}
