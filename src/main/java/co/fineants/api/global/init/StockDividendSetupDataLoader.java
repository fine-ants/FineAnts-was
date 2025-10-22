package co.fineants.api.global.init;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockDividend;
import co.fineants.stock.domain.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockDividendSetupDataLoader {

	private final FetchDividendService fetchDividendService;
	private final StockRepository stockRepository;
	
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
