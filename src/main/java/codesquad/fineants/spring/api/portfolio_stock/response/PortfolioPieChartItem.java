package codesquad.fineants.spring.api.portfolio_stock.response;

import codesquad.fineants.domain.common.money.Money;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioPieChartItem {
	private String name;
	private Money valuation;
	private Double weight;
	private Money totalGain;
	private Double totalGainRate;

	private PortfolioPieChartItem(String name, Money valuation, Double weight) {
		this(name, valuation, weight, Money.zero(), 0.0);
	}

	public static PortfolioPieChartItem stock(String name, Money valuation, Double weight, Money totalGain,
		Double totalGainRate) {
		return new PortfolioPieChartItem(
			name,
			valuation,
			weight,
			totalGain,
			totalGainRate);
	}

	public static PortfolioPieChartItem cash(Double weight, Money balance) {
		return new PortfolioPieChartItem(
			"현금",
			balance,
			weight);
	}
}
