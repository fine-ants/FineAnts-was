package co.fineants.api.domain.stock.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.fineants.api.domain.stock.domain.entity.Stock;

public interface StockRepository extends JpaRepository<Stock, Long> {

	@Query("select s from Stock s where s.isDeleted = false")
	List<Stock> findAllStocks();

	@Query("select s from Stock s where s.tickerSymbol = :tickerSymbol")
	Optional<Stock> findByTickerSymbolIncludingDeleted(@Param("tickerSymbol") String tickerSymbol);

	@Query("select distinct s from Stock s join fetch s.stockDividends sd where s.tickerSymbol in (:tickerSymbols)")
	List<Stock> findAllWithDividends(@Param("tickerSymbols") List<String> tickerSymbols);

	@Query("select s from Stock s where s.stockCode like %:keyword% or s.tickerSymbol like %:keyword% or s.companyName like %:keyword% or s.companyNameEng like %:keyword%")
	List<Stock> search(@Param("keyword") String keyword);

	@Modifying
	@Query("update Stock s set s.isDeleted = true where s.tickerSymbol in :tickerSymbols")
	int deleteAllByTickerSymbols(@Param("tickerSymbols") Set<String> tickerSymbols);
}
