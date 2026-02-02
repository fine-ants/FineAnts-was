package co.fineants.stock.domain;

import co.fineants.api.domain.common.money.Expression;
import jakarta.validation.constraints.NotNull;

public interface PriceCalculator {
	Expression calculateDailyChange(@NotNull Expression currentPrice, @NotNull Expression closingPrice);
}
