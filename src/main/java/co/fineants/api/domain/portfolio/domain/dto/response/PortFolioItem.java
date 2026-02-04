package co.fineants.api.domain.portfolio.domain.dto.response;

import java.time.LocalDateTime;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.Percentage;
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

	public static PortFolioItem of(Portfolio portfolio,
		Money totalGain, Percentage totalGainRate, Money dailyGain,
		Percentage dailyGainRate,
		Money currentValuation,
		Money currentMonthDividend) {
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
