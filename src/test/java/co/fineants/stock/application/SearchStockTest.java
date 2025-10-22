package co.fineants.stock.application;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.response.StockSearchItem;

class SearchStockTest extends AbstractContainerBaseTest {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private SearchStock searchStock;

	@DisplayName("종목 이름을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenStockName_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		stockRepository.save(TestDataFactory.createNokwonCI());

		String searchTerm = "삼성";
		// when
		List<StockSearchItem> items = searchStock.search(searchTerm);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("티커 심볼을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenTickerSymbol_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		stockRepository.save(TestDataFactory.createNokwonCI());

		String searchTerm = "005930";
		// when
		List<StockSearchItem> items = searchStock.search(searchTerm);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("영어 종목 이름을 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenStockNameEng_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		stockRepository.save(TestDataFactory.createNokwonCI());

		String searchTerm = "samsung";
		// when
		List<StockSearchItem> items = searchStock.search(searchTerm);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("키워드가 null이면 빈 리스트를 반환한다")
	@Test
	void search_whenSearchTermIsNull_ThenReturnEmptyList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		stockRepository.save(TestDataFactory.createNokwonCI());

		String searchTerm = null;
		// when
		List<StockSearchItem> items = searchStock.search(searchTerm);
		// then
		assertThat(items).isEmpty();
	}

	@DisplayName("사이즈, 키워드를 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenTickerSymbolAndSizeAndKeyword_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		String tickerSymbol = null;
		int size = 10;
		String keyword = "삼성";
		// when
		List<StockSearchItem> items = searchStock.search(tickerSymbol, size, keyword);
		// then
		assertThat(items).hasSize(1);
	}

	@DisplayName("티커 심볼과 사이즈, 키워드를 가지고 검색하면 종목 검색 아이템 리스트를 반환한다")
	@Test
	void search_givenTickerSymbolAndSizeAndKeyword2_whenSearch_thenReturnStockSearchItemList() {
		// given
		stockRepository.save(TestDataFactory.createSamsungStock());
		String tickerSymbol = "006000";
		int size = 10;
		String keyword = "삼성";
		// when
		List<StockSearchItem> items = searchStock.search(tickerSymbol, size, keyword);
		// then
		assertThat(items).hasSize(1);
	}
}
