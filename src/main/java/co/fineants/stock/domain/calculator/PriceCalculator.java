package co.fineants.stock.domain.calculator;

import java.time.LocalDate;
import java.util.List;

import co.fineants.api.domain.common.money.Expression;
import co.fineants.stock.domain.StockDividend;

public interface PriceCalculator {
	Expression calculateDailyChange(Expression currentPrice, Expression closingPrice);

	Expression calculateDailyChangeRate(Expression currentPrice, Expression closingPrice);

	Expression calculateAnnualDividend(List<StockDividend> dividends, LocalDate localDateWithNow);
}
