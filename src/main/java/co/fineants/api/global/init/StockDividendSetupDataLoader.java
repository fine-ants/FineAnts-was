package co.fineants.api.global.init;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.domain.entity.StockDividend;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.infra.s3.service.FetchDividendService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockDividendSetupDataLoader {

	private final FetchDividendService fetchDividendService;
	private final StockRepository stockRepository;

	public StockDividendSetupDataLoader(FetchDividendService fetchDividendService, StockRepository stockRepository) {
		this.fetchDividendService = fetchDividendService;
		this.stockRepository = stockRepository;
	}

	@Transactional
	public void setupStockDividends() {
		List<Stock> stocks = stockRepository.findAll();
		List<StockDividend> stockDividends = fetchDividendService.fetchDividendEntityIn(stocks);
		Map<String, List<StockDividend>> stockDividendMap = stockDividends.stream()
			.collect(Collectors.groupingBy(StockDividend::getTickerSymbol));
		for (Stock stock : stocks) {
			List<StockDividend> findStockDividends = stockDividendMap.getOrDefault(stock.getTickerSymbol(),
				List.of());
			findStockDividends.forEach(stock::addStockDividend);
		}
	}
}
