package co.fineants.api.domain.dividend.service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.KisDividend;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.infra.s3.service.FetchDividendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDividendService {

	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final KisService kisService;
	private final LocalDateTimeService localDateTimeService;
	private final ExDividendDateCalculator exDividendDateCalculator;
	private final FetchDividendService fetchDividendService;

	/**
	 * 배당일정(StockDividend) 엔티티 데이터를 초기화합니다.
	 * - 기존 배당 일정은 제거
	 * - S3로부터 배당 일정 파일(csv)을 기반으로 초기화 수행
	 * - 이 메서드는 서버 시작시 수행됨
	 */
	@Transactional
	public void initializeStockDividend() {
		List<Stock> stocks = stockRepository.findAll();
		// 기존 종목 배당금 데이터 삭제
		for (Stock stock : stocks) {
			stock.clearStockDividendTemps();
		}

		// S3에 저장된 종목 배당금으로 초기화
		List<StockDividendTemp> stockDividendTemps = fetchDividendService.fetchDividendEntityIn(stocks);
		Map<String, List<StockDividendTemp>> stockDividendMap = stockDividendTemps.stream()
			.collect(Collectors.groupingBy(StockDividendTemp::getTickerSymbol));

		// 종목에 배당금 데이터 추가
		for (Stock stock : stocks) {
			List<StockDividendTemp> findStockDividends = stockDividendMap.getOrDefault(stock.getTickerSymbol(),
				Collections.emptyList());
			findStockDividends.forEach(stock::addStockDividendTemp);
		}
	}

	/**
	 * 배당 일정 최신화
	 * <p>
	 * - 새로운 배당 일정 추가<br>
	 * - 현금 지급일 수정<br>
	 * - 범위를 벗어난 배당 일정 삭제<br>
	 *   - ex) now=202404-17 => 범위를 벗어난 배당 일정은 2023-01-01 이전 or 2024-12-31 이후<br>
	 * </p>
	 */
	@Transactional
	public void reloadStockDividend() {
		// 0. 올해 말까지의 배당 일정을 조회
		LocalDate now = localDateTimeService.getLocalDateWithNow();
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		List<KisDividend> kisDividends = kisService.fetchDividendsBetween(now, to);

		// 1. 새로운 배당 일정 탐색
		Map<String, Stock> stockMap = getStockMapBy(kisDividends);

		// 3. 새로운 배당 일정 추가
		addNewStockDividend(kisDividends, stockMap);

		// 4. 현금 지급일 수정
		updateStockDividendWithPaymentDate(kisDividends, stockMap);

		// 5. 범위를 벗어난 배당 일정을 삭제
		deleteStockDividendNotInRange(now, stockMap);
	}

	private Map<String, Stock> getStockMapBy(List<KisDividend> kisDividends) {
		List<String> tickerSymbols = kisDividends.stream()
			.map(KisDividend::getTickerSymbol)
			.toList();
		return stockRepository.findAllWithDividends(tickerSymbols)
			.stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));
	}

	private void updateStockDividendWithPaymentDate(List<KisDividend> kisDividends, Map<String, Stock> stockMap) {
		// 현금 지급일을 가지고 있지 않은 배당 일정 조회
		kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.forEach(kisDividend -> {
				StockDividendTemp stockDividend = kisDividend.getStockDividendByTickerSymbolAndRecordDateFrom(stockMap)
					.orElse(null);
				if (stockDividend == null || stockDividend.hasPaymentDate()) {
					return;
				}
				StockDividendTemp changeStockDividendTemp = kisDividend.toEntity(exDividendDateCalculator);
				if (!changeStockDividendTemp.hasPaymentDate()) {
					return;
				}
				stockDividend.change(changeStockDividendTemp);
				log.info("update StockDividendTemp with paymentDate : {}", stockDividend);
			});
	}

	private void addNewStockDividend(List<KisDividend> kisDividends, Map<String, Stock> stockMap) {
		// 배당금 정보를 stock에 추가하기
		kisDividends.stream()
			.filter(kisDividend -> kisDividend.containsFrom(stockMap))
			.filter(kisDividend -> !kisDividend.matchTickerSymbolAndRecordDateFrom(stockMap))
			.forEach(kisDividend -> {
				String tickerSymbol = kisDividend.getTickerSymbol();
				StockDividendTemp stockDividendTemp = kisDividend.toEntity(exDividendDateCalculator);
				stockMap.get(tickerSymbol).addStockDividendTemp(stockDividendTemp);
				log.info("add new StockDividendTemp : {}", stockDividendTemp);
			});
	}

	/**
	 * 작년 1월 1일부터 올해 12월 31일까지 범위에 없는 배당 일정을 제거
	 * @param now 올해의 기준일자
	 * @param stockMap 종목 맵
	 */
	private void deleteStockDividendNotInRange(LocalDate now, Map<String, Stock> stockMap) {
		int lastYear = 1;
		LocalDate from = now.minusYears(lastYear).with(TemporalAdjusters.firstDayOfYear());
		LocalDate to = now.with(TemporalAdjusters.lastDayOfYear());
		for (Stock stock : stockMap.values()) {
			List<StockDividendTemp> deleteStockDividendTemps = stock.getStockDividendNotInRange(from, to);
			deleteStockDividendTemps.forEach(stock::removeStockDividendTemp);
			log.info("delete StockDividendTemps not in range from {} to {} : {}", from, to, deleteStockDividendTemps);
		}
	}

	@Transactional(readOnly = true)
	public List<StockDividend> findAllStockDividends() {
		return stockDividendRepository.findAllStockDividends();
	}
}
