package co.fineants.api.global.init;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.stock.infrastructure.StockSpringDataJpaRepository;
import co.fineants.stock.domain.Stock;
import co.fineants.api.infra.s3.service.FetchStockService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StockSetupDataLoader {

	private final FetchStockService fetchStockService;
	private final StockSpringDataJpaRepository stockRepository;

	public StockSetupDataLoader(FetchStockService fetchStockService, StockSpringDataJpaRepository stockRepository) {
		this.fetchStockService = fetchStockService;
		this.stockRepository = stockRepository;
	}

	@Transactional
	public void setupStocks() {
		List<Stock> stocks = stockRepository.saveAll(fetchStockService.fetchStocks());
		log.info("setupStock count is {}", stocks.size());
	}
}
