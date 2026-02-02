package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;

public class StockPriceCalculator implements PriceCalculator {
	private static final Currency TARGET_CURRENCY = Currency.KRW;

	@Override
	public Expression calculateDailyChange(Expression currentPrice, Expression closingPrice) {
		return currentPrice.minus(closingPrice).reduce(Bank.getInstance(), TARGET_CURRENCY);
	}
}
