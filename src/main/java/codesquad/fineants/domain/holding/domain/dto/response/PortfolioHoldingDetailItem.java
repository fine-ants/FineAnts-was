package codesquad.fineants.domain.holding.domain.dto.response;

import java.time.LocalDateTime;

import codesquad.fineants.domain.common.count.Count;
import codesquad.fineants.domain.common.money.Bank;
import codesquad.fineants.domain.common.money.Currency;
import codesquad.fineants.domain.common.money.Expression;
import codesquad.fineants.domain.common.money.Money;
import codesquad.fineants.domain.common.money.Percentage;
import codesquad.fineants.domain.holding.domain.entity.PortfolioHolding;
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

	public static PortfolioHoldingDetailItem from(PortfolioHolding portfolioHolding, Expression lastDayClosingPrice) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		return PortfolioHoldingDetailItem.builder()
			.id(portfolioHolding.getId())
			.currentValuation(portfolioHolding.calculateCurrentValuation().reduce(bank, to))
			.currentPrice(portfolioHolding.getCurrentPrice())
			.averageCostPerShare(portfolioHolding.calculateAverageCostPerShare().reduce(bank, to))
			.numShares(portfolioHolding.calculateNumShares())
			.dailyChange(portfolioHolding.calculateDailyChange(lastDayClosingPrice).reduce(bank, to))
			.dailyChangeRate(portfolioHolding.calculateDailyChangeRate(lastDayClosingPrice)
				.toPercentage(bank, to))
			.totalGain(portfolioHolding.calculateTotalGain().reduce(bank, to))
			.totalReturnRate(portfolioHolding.calculateTotalReturnRate().toPercentage(Bank.getInstance(), to))
			.annualDividend(portfolioHolding.calculateAnnualExpectedDividend().reduce(bank, to))
			.annualDividendYield(
				portfolioHolding.calculateAnnualExpectedDividendYield().toPercentage(Bank.getInstance(), to))
			.dateAdded(portfolioHolding.getCreateAt())
			.build();
	}
}
