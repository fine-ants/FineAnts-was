package co.fineants.api.domain.stock.domain.entity;

import co.fineants.api.domain.common.count.Count;
import co.fineants.api.domain.common.money.Expression;
import co.fineants.api.domain.common.money.Money;
import co.fineants.api.domain.common.money.MoneyConverter;
import co.fineants.api.domain.dividend.domain.entity.DividendDates;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.EqualsAndHashCode;

@Embeddable
@EqualsAndHashCode(of = {"dividendDates"})
public class StockDividendTemp {
	@Convert(converter = MoneyConverter.class)
	@Column(precision = 19, nullable = false)
	private Money dividend;

	@Embedded
	private DividendDates dividendDates;

	@Column(name = "is_deleted", nullable = false)
	private boolean isDeleted;

	protected StockDividendTemp() {
	}

	public StockDividendTemp(Money dividend, DividendDates dividendDates, boolean isDeleted) {
		this.dividend = dividend;
		this.dividendDates = dividendDates;
		this.isDeleted = isDeleted;
	}

	public Expression calculateDividendSum(Count numShares) {
		return numShares.multiply(dividend);
	}
}
