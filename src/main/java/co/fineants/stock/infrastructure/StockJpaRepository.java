package co.fineants.stock.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;

public class StockJpaRepository implements StockRepository {

	@Override
	public List<Stock> findAllStocks() {
		return null;
	}

	@Override
	public Optional<Stock> findByTickerSymbolIncludingDeleted(String tickerSymbol) {
		return Optional.empty();
	}

	@Override
	public Optional<Stock> findByTickerSymbol(String tickerSymbol) {
		return Optional.empty();
	}

	@Override
	public List<Stock> findAllWithDividends(List<String> tickerSymbols) {
		return null;
	}

	@Override
	public int deleteAllByTickerSymbols(Set<String> tickerSymbols) {
		return 0;
	}
}
