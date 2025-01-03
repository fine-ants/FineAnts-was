package co.fineants.api.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
import co.fineants.api.domain.gainhistory.domain.entity.PortfolioGainHistory;
import co.fineants.api.domain.portfolio.domain.calculator.PortfolioCalculator;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
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
public class PortFolioItem {
	private Long id;
	private String securitiesFirm;
	private String name;
	private Money budget;
	private Money totalGain;
	private Percentage totalGainRate;
	private Money dailyGain;
	private Percentage dailyGainRate;
	private Money currentValuation;
	private Money expectedMonthlyDividend;
	private Count numShares;
	private LocalDateTime dateCreated;

	public static PortFolioItem of(Portfolio portfolio, PortfolioGainHistory prevHistory,
		PortfolioCalculator calculator) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		Expression totalGain = calculator.calTotalGainBy(portfolio);
		Expression totalGainRate = calculator.calTotalGainRateBy(portfolio);
		Expression totalCurrentValuation = calculator.calTotalCurrentValuationBy(portfolio);
		Expression dailyGain = calculator.calDailyGain(prevHistory, portfolio);
		Expression dailyGainRate = calculator.calDailyGainRateBy(prevHistory, portfolio);
		Expression currentMonthDividend = calculator.calCurrentMonthDividendBy(portfolio);
		return PortFolioItem.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.securitiesFirm())
			.name(portfolio.name())
			.budget(portfolio.getBudget())
			.totalGain(totalGain.reduce(bank, to))
			.totalGainRate(totalGainRate.toPercentage(Bank.getInstance(), Currency.KRW))
			.dailyGain(dailyGain.reduce(bank, to))
			.dailyGainRate(dailyGainRate.toPercentage(Bank.getInstance(), Currency.KRW))
			.currentValuation(totalCurrentValuation.reduce(bank, to))
			.expectedMonthlyDividend(currentMonthDividend.reduce(bank, to))
			.numShares(portfolio.numberOfShares())
			.dateCreated(portfolio.getCreateAt())
			.build();
	}
}
