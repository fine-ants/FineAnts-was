package co.fineants.api.domain.stock.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.stock.service.StockCsvParser;
import co.fineants.stock.domain.Stock;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class StockQueryRepositoryTest extends AbstractContainerBaseTest {

	@Autowired
	private StockQueryRepository repository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockCsvParser stockCsvParser;

	private String lastTickerSymbol;

	@BeforeEach
	void setup() {
		try {
			InputStream inputStream = new ClassPathResource("stocks.csv").getInputStream();
			stockRepository.saveAll(stockCsvParser.parse(inputStream));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@DisplayName("사용자는 키워드 없이 종목을 검색한다")
	@Test
	void getSliceOfStock() {
		// given
		String tickerSymbol = null;
		int size = 10;
		String keyword = null;
		// when
		List<Stock> stocks = repository.getSliceOfStock(tickerSymbol, size, keyword);
		// then
		Assertions.assertThat(stocks).hasSize(10);
	}

	@DisplayName("종목 검색 시나리오")
	@TestFactory
	Collection<DynamicTest> createGetSliceOfStockTest() {
		return List.of(
			DynamicTest.dynamicTest("사용자는 키워드로 삼성을 입력하고 종목 검색을 한다", () -> {
				// given
				String tickerSymbol = null;
				int size = 10;
				String keyword = "삼성";
				// when
				List<Stock> stocks = repository.getSliceOfStock(tickerSymbol, size, keyword);
				// then
				stocks.forEach(s -> log.debug("stock : {}", s));
				Assertions.assertThat(stocks).hasSize(10);

				lastTickerSymbol = stocks.get(stocks.size() - 1).getTickerSymbol();
			}),
			DynamicTest.dynamicTest("사용자는 스크롤을 하여 추가적인 종목 검색을 한다", () -> {
				// given
				int size = 10;
				String keyword = "삼성";
				// when
				List<Stock> stocks = repository.getSliceOfStock(lastTickerSymbol, size, keyword);
				// then
				stocks.forEach(s -> log.debug("stock : {}", s));
				Assertions.assertThat(stocks).hasSize(10);

				lastTickerSymbol = stocks.get(stocks.size() - 1).getTickerSymbol();
			}),
			DynamicTest.dynamicTest("사용자는 스크롤을 하여 남은 종목 전부를 검색한다", () -> {
				// given
				int size = 10;
				String keyword = "삼성";
				// when
				List<Stock> stocks = repository.getSliceOfStock(lastTickerSymbol, size, keyword);
				// then
				stocks.forEach(s -> log.debug("stock : {}", s));
				Assertions.assertThat(stocks).hasSize(5);

				lastTickerSymbol = stocks.get(stocks.size() - 1).getTickerSymbol();
			})
		);
	}
}
