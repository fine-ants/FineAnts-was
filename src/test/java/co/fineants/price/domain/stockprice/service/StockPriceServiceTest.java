package co.fineants.price.domain.stockprice.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.repository.WebSocketApprovalKeyRedisRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock.service.StockCsvReader;
import co.fineants.price.domain.stockprice.repository.StockPriceRepository;

class StockPriceServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockPriceService stockPriceService;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private WebSocketApprovalKeyRedisRepository webSocketApprovalKeyRedisRepository;
	
	@AfterEach
	void tearDown() {
		stockPriceRepository.clear();
	}

	@DisplayName("사용자는 종목 정보를 큐에 넣는다")
	@Test
	void pushStocks() {
		// given
		webSocketApprovalKeyRedisRepository.saveApprovalKey("approvalKey");
		Set<String> tickerSymbols = Set.of("005930", "035720");
		// when
		stockPriceService.pushStocks(tickerSymbols);
		// then
		Assertions.assertThat(stockPriceRepository.findAll()).hasSize(2);
	}

	@DisplayName("사용자는 100개의 종목을 요청하면 구독할 수 있는 종목은 최대 20개이다")
	@Test
	void pushStocks_whenPushLargeStocks_thenMaximumSubscribeIs20() {
		// given
		webSocketApprovalKeyRedisRepository.saveApprovalKey("approvalKey");
		Set<String> tickers = saveStocks().stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		// when
		stockPriceService.pushStocks(tickers);
		// then
		Assertions.assertThat(stockPriceRepository.size()).isEqualTo(20);
	}

	private List<Stock> saveStocks() {
		return stockRepository.saveAll(stockCsvReader.readStockCsv()
			.stream()
			.limit(100)
			.toList());
	}
}
