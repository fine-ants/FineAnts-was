package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Money;

public class StockPriceCalculator implements PriceCalculator {
	private static final Currency TARGET_CURRENCY = Currency.KRW;

	@Override
	public Money calculateDailyChange(Money currentPrice, Money closingPrice) {
		return currentPrice.minus(closingPrice).reduce(Bank.getInstance(), TARGET_CURRENCY);
	}
}
