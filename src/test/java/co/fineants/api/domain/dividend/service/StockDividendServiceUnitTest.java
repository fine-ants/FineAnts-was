package co.fineants.api.domain.dividend.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;

@ExtendWith(MockitoExtension.class)
class StockDividendServiceUnitTest {
	@Mock
	private StockRepository stockRepository;

	@Mock
	private LocalDateTimeService localDateTimeService;

	@Mock
	private ExDividendDateCalculator exDividendDateCalculator;

	@Mock
	private KisService kisService;

	@Mock
	private FetchDividendService fetchDividendService;

	@InjectMocks
	private StockDividendService stockDividendService;

	@DisplayName("종목 배당 데이터를 초기화하면 종목 데이터에 배당 데이터가 추가되어야 하고 데이터베이스에 저장되어야 한다")
	@Test
	void should_add_stock_dividends_to_stock_when_initialize_stock_dividends_then_save_to_db() {
		// given
		Stock samsung = TestDataFactory.createSamsungStock();
		Stock kakao = TestDataFactory.createKakaoStock();
		List<Stock> stocks = List.of(samsung, kakao);
		List<StockDividend> samsungStockDividends = TestDataFactory.createSamsungStockDividends();
		List<StockDividend> kakaoStockDividends = TestDataFactory.createKakaoStockDividends();
		List<StockDividend> stockDividends = Stream.of(samsungStockDividends, kakaoStockDividends)
			.flatMap(Collection::stream)
			.toList();
		BDDMockito.given(stockRepository.findAll())
			.willReturn(stocks);
		BDDMockito.given(fetchDividendService.fetchDividendEntityIn(stocks))
			.willReturn(stockDividends);
		// when
		stockDividendService.initializeStockDividend();
		// then
		assertThat(samsung.getStockDividends()).containsExactlyElementsOf(samsungStockDividends);
		assertThat(kakao.getStockDividends()).containsExactlyElementsOf(kakaoStockDividends);
	}

	@DisplayName("배당 일정을 최신화하면 새로운 배당 일정을 추가하고 기존 배당 데이터의 현금 지급일을 수정하고 범위를 벗어난 배당 일정을 삭제해야 한다")
	@Test
	void should_add_new_stock_dividends_and_update_payment_date_and_delete_stock_dividends_not_in_range() {
		// given
		Stock samsung = TestDataFactory.createSamsungStock();
		List<StockDividend> samsungStockDividends = TestDataFactory.createSamsungStockDividends();
		samsungStockDividends.forEach(samsung::addStockDividend);
		Stock kakao = TestDataFactory.createKakaoStock();
		List<StockDividend> kakaoStockDividends = TestDataFactory.createKakaoStockDividends();
		kakaoStockDividends.forEach(kakao::addStockDividend);
		List<Stock> stocks = List.of(samsung, kakao);

		LocalDate from = LocalDate.of(2024, 4, 17);
		LocalDate to = from.with(TemporalAdjusters.lastDayOfYear());
		BDDMockito.given(localDateTimeService.getLocalDateWithNow())
			.willReturn(from);
		BDDMockito.given(kisService.fetchDividendsBetween(
			from,
			to
		)).willReturn(createKisDividends());
		BDDMockito.given(stockRepository.findAllWithDividends(ArgumentMatchers.anyList()))
			.willReturn(stocks);
		BDDMockito.given(exDividendDateCalculator.calculate(LocalDate.of(2024, 2, 29)))
			.willReturn(LocalDate.of(2024, 2, 28));
		BDDMockito.given(exDividendDateCalculator.calculate(LocalDate.of(2024, 3, 31)))
			.willReturn(LocalDate.of(2024, 3, 29));
		BDDMockito.given(exDividendDateCalculator.calculate(LocalDate.of(2024, 6, 30)))
			.willReturn(LocalDate.of(2024, 6, 28));
		// when
		stockDividendService.reloadStockDividend();

		// then
		assertThat(samsung.getStockDividends())
			.hasSize(6)
			.map(this::parseStockDividend)
			.containsExactly(
				"005930:₩361:2023-03-31:2023-03-30:2023-05-17",
				"005930:₩361:2023-06-30:2023-06-29:2023-08-16",
				"005930:₩361:2023-09-30:2023-09-27:2023-11-20",
				"005930:₩361:2023-12-31:2023-12-28:2024-04-19",
				"005930:₩361:2024-03-31:2024-03-29:2024-05-17",
				"005930:₩361:2024-06-30:2024-06-28:null"
			);
		assertThat(kakao.getStockDividends())
			.hasSize(1)
			.map(this::parseStockDividend)
			.containsExactly(
				"035720:₩61:2024-02-29:2024-02-28:null"
			);
	}

	private String parseStockDividend(StockDividend stockDividend) {
		String dividendDateString = String.format("%s:%s:%s", stockDividend.getRecordDate(),
			stockDividend.getExDividendDate(), stockDividend.getPaymentDate());
		return String.format("%s:%s:%s", stockDividend.getTickerSymbol(), stockDividend.getDividend(),
			dividendDateString);
	}

	/**
	 * KisDividend 리스트 데이터 생성
	 * - 새로운 배정 기준일 생성
	 * - 기존 데이터에 현금 배당 지급일 새로 할당
	 * @return KisDividend 타입의 리스트
	 */
	private List<KisDividend> createKisDividends() {
		String samsungTickerSymbol = "005930";
		int samsungDividend = 361;
		String kakaoTickerSymbol = "035720";
		int kakaoDividend = 61;
		return List.of(
			KisDividend.create(
				samsungTickerSymbol,
				Money.won(samsungDividend),
				LocalDate.of(2023, 3, 31),
				LocalDate.of(2023, 5, 17)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.won(samsungDividend),
				LocalDate.of(2023, 6, 30),
				LocalDate.of(2023, 8, 16)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.won(samsungDividend),
				LocalDate.of(2023, 9, 30),
				LocalDate.of(2023, 11, 20)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.won(samsungDividend),
				LocalDate.of(2023, 12, 31),
				LocalDate.of(2024, 4, 19)
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.won(samsungDividend),
				LocalDate.of(2024, 3, 31),
				LocalDate.of(2024, 5, 17) // 기존 데이터에서 새로운 현금 배당 지급일이 할당된 경우
			),
			KisDividend.create(
				samsungTickerSymbol,
				Money.won(samsungDividend),
				LocalDate.of(2024, 6, 30), // 새로운 배당 기준일이 생긴 경우
				null
			),
			KisDividend.create(
				kakaoTickerSymbol,
				Money.won(kakaoDividend),
				LocalDate.of(2024, 2, 29),
				null
			)
		);
	}
}
