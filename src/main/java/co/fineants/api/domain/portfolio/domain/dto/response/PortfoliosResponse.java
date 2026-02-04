package co.fineants.api.domain.portfolio.domain.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
public class PortfoliosResponse {
	private List<PortFolioItem> portfolios;

	public static PortfoliosResponse of(List<Portfolio> portfolios,
		Map<Portfolio, PortfolioGainHistory> portfolioGainHistoryMap,
		PortfolioCalculator calculator) {
		Bank bank = Bank.getInstance();
		Currency to = Currency.KRW;
		List<PortFolioItem> items = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			PortfolioGainHistory prevHistory = portfolioGainHistoryMap.get(portfolio);
			Money totalGain = calculator.calTotalGainBy(portfolio).reduce(bank, to);
			Percentage totalGainRate = calculator.calTotalGainRateBy(portfolio)
				.toPercentage(bank, to);
			Money dailyGain = calculator.calDailyGain(prevHistory, portfolio).reduce(bank, to);
			Percentage dailyGainRate = calculator.calDailyGainRateBy(prevHistory, portfolio).toPercentage(bank, to);
			Money currentValuation = calculator.calTotalCurrentValuationBy(portfolio).reduce(bank, to);
			Money currentMonthDividend = calculator.calCurrentMonthDividendBy(portfolio).reduce(bank, to);

			PortFolioItem item = PortFolioItem.builder()
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
			items.add(item);
		}
		return new PortfoliosResponse(items);
	}
}
