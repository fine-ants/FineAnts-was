package co.fineants.api.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.holding.domain.entity.PortfolioHolding;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class PortfolioHoldingDetailItem {
	private Long id;
	private Money currentValuation;
	private Money currentPrice;
	private Money averageCostPerShare;
	private Count numShares;
	private Money dailyChange;
	private Percentage dailyChangeRate;
	private Money totalGain;
	private Percentage totalReturnRate;
	private Money annualDividend;
	private Percentage annualDividendYield;
	private LocalDateTime dateAdded;

	public static PortfolioHoldingDetailItem from(PortfolioHolding holding, Expression closingPrice,
		PortfolioCalculator calculator) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(holding);
		Expression currentPrice = calculator.fetchCurrentPrice(holding);
		Expression averageCostPerShare = calculator.calAverageCostPerShareBy(holding);
		Count numShares = calculator.calNumSharesBy(holding);
		Expression annualDividendYield = calculator.calAnnualExpectedDividendYieldBy(holding);
		Expression dailyChange = calculator.calDailyChange(holding, closingPrice);
		Expression dailyChangeRate = calculator.calDailyChangeRate(holding, closingPrice);
		Expression totalGain = calculator.calTotalGainBy(holding);
		Percentage totalReturnPercentage = calculator.calTotalGainPercentage(holding);
		Expression annualExpectedDividend = calculator.calAnnualExpectedDividendBy(holding);
		return PortfolioHoldingDetailItem.builder()
			.id(holding.getId())
			.currentValuation(toWon(totalCurrentValuation))
			.currentPrice(toWon(currentPrice.reduce(bank, to)))
			.averageCostPerShare(toWon(averageCostPerShare))
			.numShares(numShares)
			.dailyChange(toWon(dailyChange))
			.dailyChangeRate(toPercentage(dailyChangeRate))
			.totalGain(toWon(totalGain))
			.totalReturnRate(totalReturnPercentage)
			.annualDividend(toWon(annualExpectedDividend))
			.annualDividendYield(toPercentage(annualDividendYield))
			.dateAdded(holding.getCreateAt())
			.build();
	}

	private static Money toWon(Expression expression) {
		return expression.reduce(Bank.getInstance(), Currency.KRW);
	}

	private static Percentage toPercentage(Expression expression) {
		return expression.toPercentage(Bank.getInstance(), Currency.KRW);
	}
}
