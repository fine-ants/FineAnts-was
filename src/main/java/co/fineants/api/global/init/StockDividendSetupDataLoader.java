package co.fineants.api.global.init;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.infra.s3.service.FetchDividendService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockDividendSetupDataLoader {

	private final FetchDividendService fetchDividendService;
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;

	public StockDividendSetupDataLoader(FetchDividendService fetchDividendService, StockRepository stockRepository,
		StockDividendRepository stockDividendRepository) {
		this.fetchDividendService = fetchDividendService;
		this.stockRepository = stockRepository;
		this.stockDividendRepository = stockDividendRepository;
	}

	@Transactional
	public void setupStockDividends() {
		List<StockDividend> stockDividends = fetchDividendService.fetchDividendEntityIn(stockRepository.findAll());
		List<StockDividend> savedStockDividends = new ArrayList<>();
		for (StockDividend stockDividend : stockDividends) {
			if (stockDividendRepository.findByTickerSymbolAndRecordDate(stockDividend.getStock().getTickerSymbol(),
				stockDividend.getDividendDates().getRecordDate()).isEmpty()) {
				StockDividend saveStockDividend = stockDividendRepository.save(stockDividend);
				savedStockDividends.add(saveStockDividend);
			}
		}
		log.info("saved StockDividends count is {}", savedStockDividends.size());
	}
}
