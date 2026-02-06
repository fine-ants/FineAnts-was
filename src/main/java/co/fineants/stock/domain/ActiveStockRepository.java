package co.fineants.stock.domain;

import java.util.Collection;
import java.util.Set;

public interface ActiveStockRepository {
	String ACTIVE_STOCKS_KEY = "active_stocks";

	void markStockAsActive(String tickerSymbol);

	void markStocksAsActive(Collection<String> tickerSymbols);

	Set<String> getActiveStockTickerSymbols(long minutesAgo);

	void cleanupInactiveStocks(long minutesAgo);
}
