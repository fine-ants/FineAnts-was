package co.fineants.stock.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import co.fineants.stock.domain.Stock;
import co.fineants.stock.domain.StockRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StockJpaRepository implements StockRepository {

	private final StockSpringDataJpaRepository repository;

	@Override
	public List<Stock> findAllStocks() {
		return repository.findAll();
	}

	@Override
	public Optional<Stock> findByTickerSymbolIncludingDeleted(String tickerSymbol) {
		return repository.findByTickerSymbolIncludingDeleted(tickerSymbol);
	}

	@Override
	public Optional<Stock> findByTickerSymbol(String tickerSymbol) {
		return repository.findByTickerSymbol(tickerSymbol);
	}

	@Override
	public List<Stock> findAllWithDividends(List<String> tickerSymbols) {
		return repository.findAllWithDividends(tickerSymbols);
	}

	@Override
	public int deleteAllByTickerSymbols(Set<String> tickerSymbols) {
		return repository.deleteAllByTickerSymbols(tickerSymbols);
	}
}
