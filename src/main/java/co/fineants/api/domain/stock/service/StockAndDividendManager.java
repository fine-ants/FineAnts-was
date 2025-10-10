package co.fineants.api.domain.stock.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.calculator.ExDividendDateCalculator;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.kis.domain.dto.response.DividendItem;
import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.domain.stock.domain.dto.response.StockDataResponse;
import co.fineants.api.domain.stock.domain.dto.response.StockReloadResponse;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividendTemp;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.common.delay.DelayManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockAndDividendManager {
	private final StockRepository stockRepository;
	private final StockDividendRepository dividendRepository;
	private final KisService kisService;
	private final DelayManager delayManager;
	private final ExDividendDateCalculator exDividendDateCalculator;

	/**
	 * 한국 투자 증권 서버에 조회 후 종목 및 배당 일정 최신화 수행
	 * 종목 및 배당 일정 최신화 내용은 다음과 같다
	 * - 신규 상장 종목 저장
	 * - 상장 폐지 종목 및 종목의 배당 일정 삭제
	 *  - 삭제 처리는 소프트 삭제 처리
	 * - 올해 신규 배당 일정 저장
	 * @return StockRefreshResponse - 신규 상장 종목, 상장 페지 종목, 올해 신규 배당 일정
	 */
	@Transactional
	public StockReloadResponse reloadStocks() {
		// 신규 상장 종목 저장
		List<Stock> ipoStocks = fetchIpoStocks();
		List<Stock> saveIpoStocks = stockRepository.saveAll(ipoStocks);
		Set<String> addedStocks = saveIpoStocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());

		// 상장 폐지 종목 조회
		Map<Boolean, List<Stock>> partitionedStocksForDelisted = fetchPartitionedStocksForDelisted()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyMap);

		// 상장 폐지 종목 및 종목의 배당 일정 삭제
		Set<String> deletedStocks = deleteStocks(partitionedStocksForDelisted.get(true));

		// 올해 신규 배당 일정 저장
		List<StockDividendTemp> addedDividends = fetchDividend(partitionedStocksForDelisted.get(false));

		// 신규 배당일정을 종목에 추가
		List<Stock> allStocks = stockRepository.findAll();
		Map<String, Stock> stockMap = allStocks.stream()
			.collect(Collectors.toMap(Stock::getTickerSymbol, stock -> stock));
		for (StockDividendTemp dividend : addedDividends) {
			String dividendTickerSymbol = dividend.getTickerSymbol();
			if (!stockMap.containsKey(dividendTickerSymbol)) {
				continue;
			}
			Stock findStock = stockMap.get(dividendTickerSymbol);
			findStock.addStockDividendTemp(dividend);
		}

		// 배당 일정 매핑
		Set<DividendItem> addedDividendsItems = mapDividendItems(addedDividends);
		return StockReloadResponse.create(addedStocks, deletedStocks, addedDividendsItems);
	}

	@NotNull
	private List<StockDividendTemp> fetchDividend(List<Stock> stocks) {
		return Stream.of(stocks)
			.map(this::mapTickerSymbols)
			.map(this::fetchDividend)
			.flatMap(Collection::stream)
			.toList();
	}

	@NotNull
	private Set<String> deleteStocks(List<Stock> stocks) {
		Set<String> deleteTickerSymbols = stocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
		return this.deleteStocks(deleteTickerSymbols);
	}

	/**
	 * 신규 상장 종목 저장
	 * 수행과정은 다음과 같습니다.
	 * - 신규 상장 종목 조회
	 * - 신규 상장 종목 저장
	 * @return 신규 상장 종목 티커 심볼
	 */
	@NotNull
	private List<Stock> fetchIpoStocks() {
		return kisService.fetchStockInfoInRangedIpo()
			.map(StockDataResponse.StockIntegrationInfo::toEntity)
			.onErrorResume(throwable -> {
				log.error("fetchStockInfoInRangedIpo error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.collectList()
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList);
	}

	private Set<DividendItem> mapDividendItems(List<StockDividendTemp> stockDividends) {
		return stockDividends.stream()
			.map(DividendItem::from)
			.collect(Collectors.toUnmodifiableSet());
	}

	@NotNull
	private Set<String> mapTickerSymbols(List<Stock> stocks) {
		return stocks.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * 종목 삭제
	 * 종목 삭제시 종목에 포함된 배당 일정을 사전에 제거해야 한다
	 * @param tickerSymbols 삭제할 종목의 티커 심볼
	 */
	private Set<String> deleteStocks(Set<String> tickerSymbols) {
		// 종목의 배당금 삭제
		int deletedDividendCount = dividendRepository.deleteByTickerSymbols(tickerSymbols);
		log.info("delete dividends for TickerSymbols : {}, deleteCount={}", tickerSymbols, deletedDividendCount);

		// 종목 삭제
		int deletedStockCount = stockRepository.deleteAllByTickerSymbols(tickerSymbols);
		log.info("delete stocks for TickerSymbols : {}, deleteCount={}", tickerSymbols, deletedStockCount);

		return tickerSymbols;
	}

	/**
	 * 상장 폐지 종목 조회
	 * 수행 과정
	 * - 종목 티커 전체 조회
	 * - 한국 투자 증권에 종목 조회
	 * - 상장 폐지 종목 분할하여 반환
	 * @return 상장 폐지 종목 분할 맵
	 */
	private Mono<Map<Boolean, List<Stock>>> fetchPartitionedStocksForDelisted() {
		final int concurrency = 20;
		return Flux.fromIterable(findAllTickerSymbols())
			.flatMap(kisService::fetchSearchStockInfo, concurrency)
			.delayElements(delayManager.delay())
			.onErrorResume(throwable -> {
				log.error("fetchPartitionedStocksForDelisted error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.collect(Collectors.partitioningBy(
					KisSearchStockInfo::isDelisted,
					Collectors.mapping(KisSearchStockInfo::toEntity, Collectors.toList())
				)
			);
	}

	@NotNull
	private Set<String> findAllTickerSymbols() {
		return stockRepository.findAllStocks()
			.stream()
			.map(Stock::getTickerSymbol)
			.collect(Collectors.toUnmodifiableSet());
	}

	/**
	 * 신규 배당 일정 조회
	 * 수행 과정
	 * - 배당 일정 조회
	 * - 배당 일정 저장
	 * @param tickerSymbols 배당 일정을 조회할 종목의 티커 심볼
	 * @return 배당 일정
	 */
	private List<StockDividendTemp> fetchDividend(Set<String> tickerSymbols) {
		// 올해 배당 일정 조회
		int concurrency = 20;
		// 배당 일정 반환
		return Flux.fromIterable(tickerSymbols)
			.flatMap(kisService::fetchDividend, concurrency)
			.delayElements(delayManager.delay())
			.collectList()
			.onErrorResume(throwable -> {
				log.error("fetchDividend error message is {}", throwable.getMessage());
				return Mono.empty();
			})
			.blockOptional(delayManager.timeout())
			.orElseGet(Collections::emptyList).stream()
			.map(dividend -> dividend.toEntity(exDividendDateCalculator))
			.toList();
	}
}
