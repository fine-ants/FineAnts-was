package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Expression;

public class StockPriceCalculator implements PriceCalculator {
	@Override
	public Expression calculateDailyChange(Expression currentPrice, Expression closingPrice) {
		return currentPrice.minus(closingPrice);
	}
}
