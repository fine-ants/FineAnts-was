package co.fineants.api.domain.holding.domain.dto.response;

import co.fineants.api.domain.common.money.Money;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class PortfolioDividendChartItem implements Comparable<PortfolioDividendChartItem> {
	private int month;
	private Money amount;

	public static PortfolioDividendChartItem empty(int month) {
		return create(month, Money.zero());
	}

	public static PortfolioDividendChartItem create(int month, Money amount) {
		return new PortfolioDividendChartItem(
			month,
			amount
		);
	}

	@Override
	public int compareTo(@NotNull PortfolioDividendChartItem item) {
		return Integer.compare(this.month, item.month);
	}
}
