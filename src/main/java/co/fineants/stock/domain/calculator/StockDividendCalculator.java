package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.List;

import co.fineants.stock.domain.StockDividend;

public class StockDividendCalculator implements DividendCalculator {
	@Override
	public List<Integer> calculateDividendMonths(List<StockDividend> dividends, LocalDate baseDate) {
		return null;
	}
}
