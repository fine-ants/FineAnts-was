package co.fineants.api.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Bank;
import co.fineants.api.domain.common.money.Currency;
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
		Money totalGain = calculator.calTotalGainBy(portfolio).reduce(bank, to);
		Percentage totalGainRate = calculator.calTotalGainRateBy(portfolio)
			.toPercentage(bank, to);
		Money dailyGain = calculator.calDailyGain(prevHistory, portfolio).reduce(bank, to);
		Percentage dailyGainRate = calculator.calDailyGainRateBy(prevHistory, portfolio).toPercentage(bank, to);
		Money currentValuation = calculator.calTotalCurrentValuationBy(portfolio).reduce(bank, to);
		Money currentMonthDividend = calculator.calCurrentMonthDividendBy(portfolio).reduce(bank, to);
		return PortFolioItem.builder()
			.id(portfolio.getId())
			.securitiesFirm(portfolio.securitiesFirm())
			.name(portfolio.name())
			.budget(portfolio.getBudget())
			.totalGain(totalGain)
			.totalGainRate(totalGainRate)
			.dailyGain(dailyGain)
			.dailyGainRate(dailyGainRate)
			.currentValuation(currentValuation)
			.expectedMonthlyDividend(currentMonthDividend)
			.numShares(portfolio.numberOfShares())
			.dateCreated(portfolio.getCreateAt())
			.build();
	}
}
