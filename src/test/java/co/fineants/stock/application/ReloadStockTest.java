package co.fineants.stock.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.stock.domain.Market;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.response.StockDataResponse;
import co.fineants.stock.presentation.dto.response.StockReloadResponse;
import jakarta.persistence.EntityManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ReloadStockTest extends AbstractContainerBaseTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private DelayManager spyDelayManager;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@Autowired
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		BDDMockito.given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 1, 1));
	}

	@Transactional
	@DisplayName("상장된 종목과 폐지된 종목을 조회하여 최신화한다")
	@Test
	void reloadStocks() {
		// given
		Stock nokwon = stockRepository.save(TestDataFactory.createNokwonCI());

		StockDataResponse.StockIntegrationInfo hynix = StockDataResponse.StockIntegrationInfo.create(
			"000660",
			"에스케이하이닉스보통주",
			"SK hynix",
			"KR7000660001",
			"전기,전자",
			Market.KOSPI);
		given(mockedKisService.fetchStockInfoInRangedIpo())
			.willReturn(Flux.just(hynix));
		given(mockedKisService.fetchSearchStockInfo(hynix.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.listedStock(
				"KR7000660001",
				"000660",
				"에스케이하이닉스보통주",
				"SK hynix",
				"STK",
				"시가총액규모대",
				"전기,전자",
				"전기,전자"))
			);
		given(mockedKisService.fetchSearchStockInfo(nokwon.getTickerSymbol()))
			.willReturn(Mono.just(KisSearchStockInfo.delistedStock("KR7065560005", "065560", "녹원씨엔아이",
				"Nokwon Commercials & Industries, Inc.",
				"KSQ", "시가총액규모대", "소프트웨어", "소프트웨어", LocalDate.of(2024, 7, 29))));
		DateTimeFormatter dtf = DateTimeFormatter.BASIC_ISO_DATE;
		given(mockedKisService.fetchDividend(hynix.getTickerSymbol()))
			.willReturn(Flux.just(KisDividend.create(hynix.getTickerSymbol(),
					Money.won(300),
					LocalDate.parse("20240331", dtf),
					LocalDate.parse("20240514", dtf)),
				KisDividend.create(hynix.getTickerSymbol(),
					Money.won(300),
					LocalDate.parse("20240630", dtf),
					LocalDate.parse("20240814", dtf))));
		given(spyDelayManager.delay()).willReturn(Duration.ZERO);
		// when
		StockReloadResponse response = stockService.reloadStocks();
		// then
		entityManager.flush();
		entityManager.clear();

		assertThat(response).isNotNull();
		assertThat(response.getAddedStocks()).hasSize(1);
		assertThat(response.getDeletedStocks()).hasSize(1);

		Stock deletedStock = stockRepository.findByTickerSymbolIncludingDeleted(nokwon.getTickerSymbol()).orElseThrow();
		assertThat(deletedStock.isDeleted()).isTrue();

		Stock findHynix = stockRepository.findByTickerSymbol(hynix.getTickerSymbol()).orElseThrow();
		assertThat(findHynix.getStockDividends())
			.hasSize(2)
			.extracting(StockDividend::getDividend, StockDividend::getDividendDates)
			.usingComparatorForType(Money::compareTo, Money.class)
			.containsExactly(
				Tuple.tuple(
					Money.won(300),
					DividendDates.of(
						LocalDate.parse("20240331", dtf),
						LocalDate.parse("20240329", dtf),
						LocalDate.parse("20240514", dtf)
					)
				),
				Tuple.tuple(
					Money.won(300),
					DividendDates.of(
						LocalDate.parse("20240630", dtf),
						LocalDate.parse("20240628", dtf),
						LocalDate.parse("20240814", dtf)
					)
				)
			);
	}

	@DisplayName("종목 최신화 수행중에 별도의 스레드에서 blocking되면 안된다")
	@Test
	void reloadStocks_shouldNotBlockingThread_whenFetchSearchStockInfo() {
		// given
		given(mockedKisService.fetchStockInfoInRangedIpo())
			.willReturn(Flux.error(
				new IllegalStateException("blockOptional() is blocking, which is not supported in thread parallel-1")));
		given(mockedKisService.fetchSearchStockInfo(anyString()))
			.willReturn(Mono.error(
				new IllegalStateException("blockOptional() is blocking, which is not supported in thread parallel-1")));
		given(mockedKisService.fetchDividend(anyString()))
			.willReturn(Flux.error(
				new IllegalStateException("blockOptional() is blocking, which is not supported in thread parallel-1")));
		// when
		StockReloadResponse response = stockService.reloadStocks();
		// then
		assertThat(response.getAddedStocks()).isEmpty();
		assertThat(response.getDeletedStocks()).isEmpty();
		assertThat(response.getAddedDividends()).isEmpty();
	}
}
