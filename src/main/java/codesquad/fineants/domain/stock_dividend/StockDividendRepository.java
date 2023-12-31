package codesquad.fineants.domain.stock_dividend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockDividendRepository extends JpaRepository<StockDividend, Long> {

	@Modifying
	@Query("DELETE FROM StockDividend sd WHERE sd.stock.tickerSymbol = :tickerSymbol")
	void deleteByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);
}
