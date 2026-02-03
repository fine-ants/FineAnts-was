package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.stock.domain.StockDividend;

@Component
public class StockPriceCalculator implements PriceCalculator {
	@Override
	public Expression calculateDailyChange(Expression currentPrice, Expression closingPrice) {
		Objects.requireNonNull(currentPrice, "Current price must not be null");
		Objects.requireNonNull(closingPrice, "Closing price must not be null");
		return currentPrice.minus(closingPrice);
	}

	@Override
	public Expression calculateDailyChangeRate(Expression currentPrice, Expression closingPrice) {
		Objects.requireNonNull(currentPrice, "Current price must not be null");
		Objects.requireNonNull(closingPrice, "Closing price must not be null");
		Expression dailyChange = calculateDailyChange(currentPrice, closingPrice);
		return dailyChange.divide(closingPrice);
	}

	@Override
	public Expression calculateAnnualDividend(List<StockDividend> stockDividends, LocalDate baseDate) {
		Objects.requireNonNull(stockDividends, "Stock dividends must not be null");
		return stockDividends.stream()
			.filter(dividend -> dividend.isCurrentYearPaymentDate(baseDate))
			.map(StockDividend::getDividend)
			.map(Expression.class::cast)
			.reduce(Money.zero(), Expression::plus);
	}
}
