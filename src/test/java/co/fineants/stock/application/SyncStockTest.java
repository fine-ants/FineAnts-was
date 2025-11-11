package co.fineants.stock.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.FetchStockService;
import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import reactor.core.publisher.Mono;

class SyncStockTest extends AbstractContainerBaseTest {
	@Autowired
	private SyncStock syncStock;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private FetchStockService fetchStockService;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@BeforeEach
	void setUp() {
		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 1, 1));
	}

	@DisplayName("종목 정보를 최신 정보로 갱신한다")
	@Test
	void givenStocks_whenSyncAllStocksWithLatestData_thenUpdateLatestData() {
		// given
		Stock samsung = stockRepository.save(createSamsungStock());
		createStockDividendWith(samsung.getTickerSymbol()).forEach(samsung::addStockDividend);
		given(mockedKisService.fetchSearchStockInfo(samsung.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
				samsung.getStockCode(),
				samsung.getTickerSymbol(),
				samsung.getCompanyName(),
				samsung.getCompanyNameEng(),
				"KSQ",
				"시가총액규모대",
				"의료",
				"의료"
			)));
		// when
		List<Stock> actual = syncStock.syncAllStocks();
		// then
		assertThat(actual).hasSize(1);
		assertThat(actual.get(0).getMarket()).isEqualTo(Market.KOSDAQ);
		assertThat(actual.get(0).getSector()).isEqualTo("의료");

		actual = fetchStockService.fetchStocks();
		assertThat(actual).hasSize(1);
	}
}
