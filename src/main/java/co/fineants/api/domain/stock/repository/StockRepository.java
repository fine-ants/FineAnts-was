package co.fineants.api.domain.stock.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.stock.domain.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

	@Query("select s from Stock s where s.isDeleted = false")
	List<Stock> findAllStocks();

	@Query("select s from Stock s where s.tickerSymbol = :tickerSymbol")
	Optional<Stock> findByTickerSymbolIncludingDeleted(@Param("tickerSymbol") String tickerSymbol);

	@Query("select s from Stock s where s.tickerSymbol = :tickerSymbol and s.isDeleted = false")
	Optional<Stock> findByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);

	@Query("select distinct s from Stock s left join fetch s.stockDividends sd where s.tickerSymbol in (:tickerSymbols)")
	List<Stock> findAllWithDividends(@Param("tickerSymbols") List<String> tickerSymbols);

	@Modifying
	@Query("update Stock s set s.isDeleted = true where s.tickerSymbol in :tickerSymbols")
	int deleteAllByTickerSymbols(@Param("tickerSymbols") Set<String> tickerSymbols);
}
