package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import co.fineants.stock.domain.StockDividend;

public class StockDividendCalculator implements DividendCalculator {
	@Override
	public List<Integer> calculateDividendMonths(List<StockDividend> dividends, LocalDate baseDate) {
		Objects.requireNonNull(dividends, "dividends must not be null");
		Objects.requireNonNull(baseDate, "baseDate must not be null");
		
		return Collections.emptyList();
	}
}
