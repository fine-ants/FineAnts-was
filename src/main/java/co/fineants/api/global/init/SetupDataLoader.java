package co.fineants.api.global.init;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.dividend.domain.entity.StockDividend;
import co.fineants.api.domain.dividend.repository.StockDividendRepository;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock.repository.StockRepository;
import co.fineants.api.global.init.properties.MemberProperties;
import co.fineants.api.global.init.properties.RoleProperties;
import co.fineants.api.infra.s3.service.FetchDividendService;
import co.fineants.api.infra.s3.service.FetchStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetupDataLoader {
	private final StockRepository stockRepository;
	private final StockDividendRepository stockDividendRepository;
	private final FetchDividendService fetchDividendService;
	private final FetchStockService fetchStockService;
	private final RoleSetupDataLoader roleSetupDataLoader;
	private final RoleProperties roleProperties;
	private final MemberSetupDataLoader memberSetupDataLoader;
	private final MemberProperties memberProperties;

	@Transactional
	public void setupResources() {
		roleSetupDataLoader.setupRoles(roleProperties);
		memberSetupDataLoader.setupMembers(memberProperties);
		setupStockResources();
		setupStockDividendResources();
	}

	private void setupStockResources() {
		List<Stock> stocks = stockRepository.saveAll(fetchStockService.fetchStocks());
		log.info("setupStock count is {}", stocks.size());
	}

	private void setupStockDividendResources() {
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
