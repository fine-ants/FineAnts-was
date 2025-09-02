package co.fineants.api.domain.stock.scheduler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.dto.response.StockDataResponse;
import co.fineants.api.domain.stock.domain.entity.Market;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.domain.stock.service.StockCsvReader;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.FetchStockService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class StockSchedulerTest extends AbstractContainerBaseTest {

	@Autowired
	private StockScheduler stockScheduler;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private StockCsvReader stockCsvReader;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private DelayManager spyDelayManager;

	@Autowired
	private FetchDividendService fetchDividendService;

	@Autowired
	private FetchStockService fetchStockService;

	@DisplayName("서버는 종목들을 최신화한다")
	@Test
	void scheduledRefreshStocks() {
		// given
		List<Stock> stocks = saveStocks();
		StockDataResponse.StockIntegrationInfo stock = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		given(mockedKisService.fetchStockInfoInRangedIpo())
			.willReturn(Flux.just(stock));
		given(mockedKisService.fetchSearchStockInfo(stock.getTickerSymbol()))
			.willReturn(Mono.just(
				KisSearchStockInfo.listedStock(stock.getStockCode(), stock.getTickerSymbol(), stock.getCompanyName(),
					stock.getCompanyNameEng(), "STK", "시가총액규모대", "전기,전자", "전기,전자")));
		stocks.forEach(s -> given(mockedKisService.fetchSearchStockInfo(s.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
					s.getStockCode(),
					s.getTickerSymbol(),
					s.getCompanyName(),
					s.getCompanyNameEng(),
					"STK",
					"시가총액규모대",
					s.getSector(),
					s.getSector()
				))
			));
		stocks.forEach(s -> given(mockedKisService.fetchDividend(anyString()))
			.willReturn(Flux.empty()));
		stocks.forEach(s -> given(mockedKisService.fetchDividend(s.getTickerSymbol()))
			.willReturn(Flux.just(KisDividend.create(s.getTickerSymbol(), Money.won(300), LocalDate.of(2024, 3, 1),
					LocalDate.of(2024, 5, 1)),
				KisDividend.create(s.getTickerSymbol(), Money.won(300), LocalDate.of(2024, 5, 1),
					LocalDate.of(2024, 7, 1)))));
		given(spyDelayManager.delay()).willReturn(Duration.ZERO);
		// when
		stockScheduler.scheduledReloadStocks();
		// then
		assertThat(stockRepository.findByTickerSymbolIncludingDeleted("000660")).isPresent();
		assertThat(fetchStockService.fetchStocks())
			.as("Verify that the stock information in the stocks.csv file stored "
				+ "in s3 matches the items in the database")
			.containsExactlyInAnyOrderElementsOf(stockRepository.findAll());
		assertThat(fetchDividendService.fetchDividendEntityIn(stockRepository.findAll()))
			.as("Verify that the dividend information in the dividends.csv file stored "
				+ "in s3 matches the items in the database")
			.containsExactlyInAnyOrderElementsOf(stockDividendRepository.findAllStockDividends());
	}

	private List<Stock> saveStocks() {
		List<Stock> stocks = stockCsvReader.readStockCsv().stream()
			.limit(100)
			.toList();
		return stockRepository.saveAll(stocks);
	}
}
