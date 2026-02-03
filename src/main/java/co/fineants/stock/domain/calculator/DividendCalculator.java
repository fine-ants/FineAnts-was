package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.List;

import co.fineants.stock.domain.StockDividend;

public interface DividendCalculator {
	List<Integer> calculateDividendMonths(List<StockDividend> dividends, LocalDate baseDate);
}
