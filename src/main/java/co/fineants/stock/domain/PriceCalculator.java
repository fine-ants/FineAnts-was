package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Expression;

public interface PriceCalculator {
	Expression calculateDailyChange(Expression currentPrice, Expression closingPrice);

	Expression calculateDailyChangeRate(Expression currentPrice, Expression closingPrice);
}
