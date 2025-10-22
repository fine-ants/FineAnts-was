package co.fineants.stock.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.fineants.api.domain.kis.repository.ClosingPriceRepository;
import co.fineants.api.domain.kis.repository.CurrentPriceRedisRepository;
import co.fineants.api.domain.kis.repository.PriceRepository;
import co.fineants.api.global.common.time.LocalDateTimeService;
import co.fineants.api.global.errors.exception.business.StockNotFoundException;
import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import co.fineants.stock.infrastructure.StockQueryDslRepository;
import co.fineants.stock.presentation.dto.response.StockResponse;
import co.fineants.stock.presentation.dto.response.StockSearchItem;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchStock {

	private final StockQueryDslRepository repository;
	private final StockRepository stockRepository;
	private final PriceRepository currentPriceRepository;
	private final ClosingPriceRepository closingPriceRepository;
	private final LocalDateTimeService localDateTimeService;

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String keyword) {
		return repository.getStock(keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<StockSearchItem> search(String tickerSymbol, int size, String keyword) {
		return repository.getSliceOfStock(tickerSymbol, size, keyword).stream()
			.map(StockSearchItem::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public StockResponse findDetailedStock(String tickerSymbol) {
		Stock stock = stockRepository.findByTickerSymbolIncludingDeleted(tickerSymbol)
			.orElseThrow(() -> new StockNotFoundException(tickerSymbol));
		return StockResponse.of(stock, (CurrentPriceRedisRepository)currentPriceRepository, closingPriceRepository,
			localDateTimeService);
	}
}
