package co.fineants.stock.domain.calculator;

import java.util.Objects;

import org.springframework.stereotype.Component;

import co.fineants.api.domain.common.money.Expression;

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
}
