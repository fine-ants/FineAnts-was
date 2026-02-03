package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.purchasehistory.domain.entity.PurchaseHistory;
import co.fineants.stock.domain.StockDividend;

@Component
public class StockDividendCalculator implements DividendCalculator {
	@Override
	public List<Integer> calculateDividendMonths(List<StockDividend> dividends, LocalDate baseDate) {
		Objects.requireNonNull(dividends, "dividends must not be null");
		Objects.requireNonNull(baseDate, "baseDate must not be null");

		return dividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(baseDate))
			.map(StockDividend::getMonthByPaymentDate)
			.map(Month::getValue)
			.toList();
	}

	@Override
	public List<StockDividend> calculateCurrentMonthStockDividends(List<StockDividend> dividends, LocalDate baseDate) {
		Objects.requireNonNull(dividends, "dividends must not be null");
		Objects.requireNonNull(baseDate, "baseDate must not be null");

		return dividends.stream()
			.filter(dividend -> dividend.isCurrentMonthPaymentDate(baseDate))
			.toList();
	}

	@Override
	public Expression calCurrentMonthExpectedDividend(List<StockDividend> dividends, List<PurchaseHistory> histories) {
		return Money.zero();
	}
}
