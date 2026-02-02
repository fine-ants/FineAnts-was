package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Money;

public interface PriceCalculator {
	Money calculateDailyChange(Money currentPrice, Money closingPrice);
}
