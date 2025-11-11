package co.fineants.stock.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StockRepository {

	List<Stock> findAll();

	Optional<Stock> findByTickerSymbolIncludingDeleted(String tickerSymbol);

	Optional<Stock> findByTickerSymbol(String tickerSymbol);

	List<Stock> findAllWithDividends(List<String> tickerSymbols);

	int deleteAllByTickerSymbols(Set<String> tickerSymbols);

	List<Stock> saveAll(List<Stock> stocks);

	Stock save(Stock stock);
}
