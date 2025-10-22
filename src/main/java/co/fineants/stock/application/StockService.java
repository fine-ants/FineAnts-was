package co.fineants.stock.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.kis.domain.dto.response.KisSearchStockInfo;
import co.fineants.api.domain.kis.service.KisService;
import co.fineants.api.global.common.delay.DelayManager;
import co.fineants.api.infra.s3.service.WriteDividendService;
import co.fineants.api.infra.s3.service.WriteStockService;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.presentation.dto.response.StockReloadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class StockService {
	private final StockRepository stockRepository;
	private final ReloadStock reloadStock;
	private final KisService kisService;
	private final DelayManager delayManager;
	private final WriteDividendService writeDividendService;
	private final WriteStockService writeStockService;

	@Transactional
	public StockReloadResponse reloadStocks() {
		StockReloadResponse response = reloadStock.reloadStocks();
		log.info("refreshStocks response : {}", response);
		List<Stock> stocks = stockRepository.findAll();
		writeStockService.writeStocks(stocks);
		writeDividendService.writeDividend(toStockDividends(stocks));
		return response;
	}

	private StockDividend[] toStockDividends(List<Stock> stocks) {
		return stocks.stream()
			.flatMap(stock -> stock.getStockDividends().stream())
			.toArray(StockDividend[]::new);
	}

	/**
	 * 기존 모든 종목들을 최신 정보로 갱신한다
	 *
	 * @return 최신화된 종목 리스트
	 */
	@Transactional
	public List<Stock> syncAllStocksWithLatestData() {
		List<String> tickerSymbols = stockRepository.findAll().stream()
			.map(Stock::getTickerSymbol)
			.toList();
		List<Stock> stocks = new ArrayList<>();
		for (String ticker : tickerSymbols) {
			Mono<KisSearchStockInfo> mono = kisService.fetchSearchStockInfo(ticker);
			mono.map(KisSearchStockInfo::toEntity)
				.blockOptional(delayManager.timeout())
				.ifPresent(stock -> {
					Stock save = stockRepository.save(stock);
					stocks.add(save);
				});
		}
		writeStockService.writeStocks(stocks);
		return stocks;
	}
}
