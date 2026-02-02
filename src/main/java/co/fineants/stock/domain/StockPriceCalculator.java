package co.fineants.stock.domain;

import java.util.Objects;

import co.fineants.api.domain.common.money.Expression;

public class StockPriceCalculator implements PriceCalculator {
	@Override
	public Expression calculateDailyChange(Expression currentPrice, Expression closingPrice) {
		Objects.requireNonNull(currentPrice, "Current price must not be null");
		Objects.requireNonNull(closingPrice, "Closing price must not be null");
		return currentPrice.minus(closingPrice);
	}
}
