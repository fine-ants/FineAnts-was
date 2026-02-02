package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Expression;
import jakarta.validation.constraints.NotNull;

public class StockPriceCalculator implements PriceCalculator {
	@Override
	public Expression calculateDailyChange(@NotNull Expression currentPrice, @NotNull Expression closingPrice) {
		return currentPrice.minus(closingPrice);
	}
}
