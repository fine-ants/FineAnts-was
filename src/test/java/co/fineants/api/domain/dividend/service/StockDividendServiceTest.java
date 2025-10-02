package co.fineants.api.domain.dividend.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.TestDataFactory;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.repository.KisAccessTokenRepository;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.WriteDividendService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WithMockUser(roles = {"ADMIN"})
class StockDividendServiceTest extends AbstractContainerBaseTest {

	@Autowired
	private StockDividendService stockDividendService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockDividendRepository stockDividendRepository;

	@Autowired
	private KisAccessTokenRepository kisAccessTokenRepository;

	@Autowired
	private LocalDateTimeService spyLocalDateTimeService;

	@Autowired
	private KisService mockedKisService;

	@Autowired
	private WriteDividendService writeDividendService;

	/**
	 * 해당 테스트 수행시 localStack에 저장된 dividends.csv 파일을 이용하여 배당 일정을 초기화합니다.
	 */
	@DisplayName("배당일정을 초기화한다")
	@Test
	void initializeStockDividend() {
		// given
		Stock samsung = createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(samsung::addStockDividendTemp);
		Stock stock = stockRepository.save(samsung);
		writeDividendService.writeDividend(stock.getStockDividends());
		// when
		stockDividendService.initializeStockDividend();
		// then
		assertThat(stockDividendRepository.findAllStockDividends()).hasSize(9);
	}

	@DisplayName("배당 일정을 최신화한다")
	@Test
	void refreshStockDividend() {
		// given
		Stock samsung = createSamsungStock();
		TestDataFactory.createSamsungStockDividends().forEach(samsung::addStockDividendTemp);
		Stock kakao = createKakaoStock();
		TestDataFactory.createKakaoStockDividends().forEach(kakao::addStockDividendTemp);
		stockRepository.saveAll(List.of(samsung, kakao));

		// 새로운 배정 기준일이 생김
		// 기존 데이터에 현금 배당 지급일이 새로 할당됨
		String samsungTickerSymbol = "005930";
		int samsungDividend = 361;

		String kakaoTickerSymbol = "035720";
		int kakaoDividend = 61;

		given(mockedKisService.fetchDividendsBetween(
			ArgumentMatchers.any(LocalDate.class),
			ArgumentMatchers.any(LocalDate.class)
		)).willReturn(List.of(
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
		));
		given(spyLocalDateTimeService.getLocalDateWithNow())
			.willReturn(LocalDate.of(2024, 4, 17));
		// when
		stockDividendService.reloadStockDividend();

		// then
		List<StockDividend> stockDividends = stockDividendRepository.findAllStockDividends();
		assertThat(stockDividends)
			.hasSize(8)
			.map(StockDividend::parse)
			.containsExactlyInAnyOrder(
				"005930:₩361:2023-03-31:2023-03-30:2023-05-17",
				"005930:₩361:2023-06-30:2023-06-29:2023-08-16",
				"005930:₩361:2023-09-30:2023-09-27:2023-11-20",
				"005930:₩361:2023-12-31:2023-12-28:2024-04-19",
				"005930:₩361:2024-03-31:2024-03-29:2024-05-17", // false 2024-03-30
				"005930:₩361:2024-06-30:2024-06-28:null", // false 2024-06-29
				"035720:₩61:2022-12-31:2023-12-30:2023-04-25",
				"035720:₩61:2024-02-29:2024-02-28:null"
			);
	}
}
